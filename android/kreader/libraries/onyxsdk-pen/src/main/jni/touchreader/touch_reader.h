#ifndef TOUCH_READER_H
#define TOUCH_READER_H

#include <sys/poll.h>

class TouchReader {

public:
    static const int ON_PRESS = 0;
    static const int ON_MOVE = 1;
    static const int ON_RELEASE = 2;
    static const int ON_RELEASE_OUT_LIMIT_REGION = 3;

public:
    TouchReader();
    ~TouchReader();

public:
    typedef void(*onTouchPointReceived)(void * userData, int, int, int, long, bool, bool, bool, int);

public:
    int openDevice(const std::string& devicePath, std::string& deviceName);
    std::string findDevice();
    void closeDevice();
    void setStrokeWidth(float width);
    void setLimitRegion(float *array, int len);
    void setExcludeRegion(float *array, int len);
    void readTouchEventLoop(void *userData, onTouchPointReceived callback);

private:
    void processEvent(void *userData, onTouchPointReceived callback, int type, int code, int value, long ts);
    bool inLimitRegion(float x, float y);
    bool inExcludeRegion(float x, float y);
    void clearLimitArray();
    void clearExcludeArray();

private:
    bool debug = true;

    int fd;
    int efd;
    int nfds;
    pollfd *ufds;
    int px, py, pressure;
    int state, lastState;
    bool volatile running;
    bool volatile drawing;
    bool pressed;
    bool lastPressed;
    bool volatile erasing;
    bool shortcutDrawing;
    bool shortcutErasing;
    float *limitArray;
    int limitArrayLength;
    float *excludeArray;
    int excludeArrayLength;
    float strokeWidth;

};

#endif // TOUCH_READER_H
