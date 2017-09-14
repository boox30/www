#include "com_onyx_android_sdk_scribble_touch_RawInputReader.h"
#include "log.h"
#include "JNIUtils.h"
#include "touch_reader.h"

#include <cstdlib>
#include <jni.h>
#include <iostream>
#include <fstream>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <dirent.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <sys/inotify.h>
#include <sys/limits.h>
#include <sys/poll.h>
#include <linux/input.h>
#include <errno.h>

TouchReader::TouchReader() :
    limitArray(nullptr), excludeArray(nullptr), shortcutDrawing(false), shortcutErasing(false)
{

}

TouchReader::~TouchReader() {
    closeDevice();
    clearLimitArray();
    clearExcludeArray();
}

void TouchReader::clearLimitArray() {
    if (limitArray) {
        free(limitArray);
        limitArray = NULL;
    }
}

void TouchReader::clearExcludeArray() {
    if (excludeArray) {
        free(excludeArray);
        excludeArray = NULL;
    }
}

void TouchReader::setStrokeWidth(float width) {
    strokeWidth = width;
}

bool TouchReader::inLimitRegion(float x, float y) {
    if (!limitArray) {
        return true;
    }

    float detectStrokeWidth = strokeWidth / 2;

    for (int i = 0; i < limitArrayLength; i += 4) {
        float leftLimit = limitArray[i];
        float topLimit = limitArray[i + 1];
        float rightLimit = limitArray[i + 2];
        float bottomLimit = limitArray[i + 3];

        if (leftLimit <= (x - detectStrokeWidth) &&
            (x + detectStrokeWidth) <= rightLimit &&
            topLimit <= (y - detectStrokeWidth) &&
            (y + detectStrokeWidth) <= bottomLimit) {
            return true;
        }
    }
    return false;
}

bool TouchReader::inExcludeRegion(float x, float y) {
    if (!excludeArray) {
        return false;
    }

    float detectStrokeWidth = strokeWidth / 2;

    for (int i = 0; i < excludeArrayLength; i += 4) {
        float leftLimit = excludeArray[i];
        float topLimit = excludeArray[i + 1];
        float rightLimit = excludeArray[i + 2];
        float bottomLimit = excludeArray[i + 3];

        if (leftLimit <= (x - detectStrokeWidth) &&
            (x + detectStrokeWidth) <= rightLimit &&
            topLimit <= (y - detectStrokeWidth) &&
            (y + detectStrokeWidth) <= bottomLimit) {
            return true;
        }
    }
    return false;
}

int TouchReader::openDevice(const std::string& devicePath, std::string& deviceName) {
    int version;
    char name[80] = {0};
    char location[80] = {0};
    char idstr[80] = {0};
    struct input_id id;

    fd = open(devicePath.c_str(), O_RDONLY);
    if(fd < 0) {
        LOGE("could not open %s, %s\n", devicePath.c_str(), strerror(errno));
        return -1;
    }

    if(ioctl(fd, EVIOCGVERSION, &version)) {
        LOGE("could not get driver version for %s, %s\n", devicePath.c_str(), strerror(errno));
        return -1;
    }
    if(ioctl(fd, EVIOCGID, &id)) {
        LOGE("could not get driver id for %s, %s\n", devicePath.c_str(), strerror(errno));
        return -1;
    }
    if(ioctl(fd, EVIOCGNAME(sizeof(name) - 1), &name) < 1) {
        LOGE("could not get device name for %s, %s\n", devicePath.c_str(), strerror(errno));
        name[0] = '\0';
    }
    if(ioctl(fd, EVIOCGPHYS(sizeof(location) - 1), &location) < 1) {
        //LOGE("could not get location for %s, %s\n", devicePath.string(), strerror(errno));
        location[0] = '\0';
    }
    if(ioctl(fd, EVIOCGUNIQ(sizeof(idstr) - 1), &idstr) < 1) {
        //LOGE("could not get idstring for %s, %s\n", devicePath.string(), strerror(errno));
        idstr[0] = '\0';
    }

    deviceName = name;
    return fd;
}

void TouchReader::processEvent(void *userData, onTouchPointReceived callback, int type, int code, int value, long ts) {
    if (type == EV_ABS) {
        if (code == ABS_X) {
            px = value;
        } else if (code == ABS_Y) {
            py = value;
        } else if (code == ABS_PRESSURE) {
            pressure = value;
        }
    } else if (type == EV_SYN) {
        if (pressed) {
            if (!lastPressed) {
                if(!inLimitRegion(px, py) || inExcludeRegion(px, py)) {
                    return;
                }
                lastPressed = true;
                state = ON_PRESS;
                lastState = state;
            } else {
                state = (inLimitRegion(px, py) && !inExcludeRegion(px, py)) ? ON_MOVE : ON_RELEASE_OUT_LIMIT_REGION;
            }
        } else {
            state = ON_RELEASE;
        }
        lastPressed = state != ON_RELEASE && state != ON_RELEASE_OUT_LIMIT_REGION;
        if(lastState != ON_RELEASE && lastState != ON_RELEASE_OUT_LIMIT_REGION) {
           callback(userData, px, py, pressure, ts, erasing, shortcutDrawing, shortcutErasing, state);
        }
        lastState = state;
    } else if (type == EV_KEY) {
        if (code ==  BTN_TOUCH) {
            erasing = false;
            pressed = value;
            lastPressed = false;
            if (!pressed) {
                shortcutDrawing = false;
                shortcutErasing = false;
            }
        } else if (code == BTN_TOOL_PENCIL || code == BTN_TOOL_PEN) {
            erasing = false;
            shortcutDrawing = true;
            shortcutErasing = false;
        } else if (code == BTN_TOOL_RUBBER) {
            erasing = true;
            pressed = value;
            shortcutDrawing = false;
            shortcutErasing = true;
        }
    }
}

void TouchReader::readTouchEventLoop(void *userData, onTouchPointReceived callback){
    running = true;
    struct input_event event;
    while (running && fd > 0) {
        int res = read(fd, &event, sizeof(event));
        if (res < (int)sizeof(event)) {
            continue;
        }
        processEvent(userData, callback, event.type, event.code, event.value, event.time.tv_usec);
    }
}

std::string TouchReader::findDevice() {
    using namespace std;
    for(int i = 0; i < 3; ++i) {
        const int MAX = 80;
        char temp[MAX] = {0};
        snprintf(temp, MAX - 1, "/dev/input/event%d", i);
        string path = temp;
        string deviceName;
        int fd = openDevice(path, deviceName);
        close(fd);
        if (debug) {
            LOGI("try path %s result name %s", path.c_str(), deviceName.c_str());
        }
        if (deviceName.find("hanvon") != string::npos ||
            deviceName.find("Wacom") != string::npos) {
            return path;
        }
    }
    return "/dev/input/event1";
}

void TouchReader::closeDevice(){
    LOGI("touch reader closeDevice fd %d", fd);
    if (fd > 0) {
        close(fd);
        fd = 0;
    }
    running = false;
}

void TouchReader::setLimitRegion(float *array, int len) {
    clearLimitArray();
    limitArray = array;
    limitArrayLength = len;

    for (int i = 0; i < limitArrayLength; i += 4) {
        float left = limitArray[i];
        float top = limitArray[i + 1];
        float right = limitArray[i + 2];
        float bottom = limitArray[i + 3];

        float leftLimit = left <= right ? left : right;
        float topLimit = top <= bottom ? top : bottom;
        float rightLimit = right >= left ? right : left;
        float bottomLimit = bottom >= top ? bottom : top;

        limitArray[i] = leftLimit;
        limitArray[i + 1] = topLimit;
        limitArray[i + 2] = rightLimit;
        limitArray[i + 3] = bottomLimit;
    }
}

void TouchReader::setExcludeRegion(float *array, int len) {
    clearExcludeArray();
    excludeArray = array;
    excludeArrayLength = len;

    for (int i = 0; i < excludeArrayLength; i += 4) {
        float left = excludeArray[i];
        float top = excludeArray[i + 1];
        float right = excludeArray[i + 2];
        float bottom = excludeArray[i + 3];

        float leftLimit = left <= right ? left : right;
        float topLimit = top <= bottom ? top : bottom;
        float rightLimit = right >= left ? right : left;
        float bottomLimit = bottom >= top ? bottom : top;

        excludeArray[i] = leftLimit;
        excludeArray[i + 1] = topLimit;
        excludeArray[i + 2] = rightLimit;
        excludeArray[i + 3] = bottomLimit;
    }
}