#include "onyx_drm_decrypt.h"

#include <iostream>
#include <string>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <arpa/inet.h>

#include <openssl/rsa.h>
#include <openssl/engine.h>
#include <openssl/pem.h>
#include <openssl/evp.h>

#define LOGW std::cerr
#define LOGD std::cout

using namespace onyx;

namespace {

const static char* b64="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/" ;

// maps A=>0,B=>1..
const static unsigned char unb64[]={
  0,   0,   0,   0,   0,   0,   0,   0,   0,   0, //10
  0,   0,   0,   0,   0,   0,   0,   0,   0,   0, //20
  0,   0,   0,   0,   0,   0,   0,   0,   0,   0, //30
  0,   0,   0,   0,   0,   0,   0,   0,   0,   0, //40
  0,   0,   0,  62,   0,   0,   0,  63,  52,  53, //50
 54,  55,  56,  57,  58,  59,  60,  61,   0,   0, //60
  0,   0,   0,   0,   0,   0,   1,   2,   3,   4, //70
  5,   6,   7,   8,   9,  10,  11,  12,  13,  14, //80
 15,  16,  17,  18,  19,  20,  21,  22,  23,  24, //90
 25,   0,   0,   0,   0,   0,   0,  26,  27,  28, //100
 29,  30,  31,  32,  33,  34,  35,  36,  37,  38, //110
 39,  40,  41,  42,  43,  44,  45,  46,  47,  48, //120
 49,  50,  51,   0,   0,   0,   0,   0,   0,   0, //130
  0,   0,   0,   0,   0,   0,   0,   0,   0,   0, //140
  0,   0,   0,   0,   0,   0,   0,   0,   0,   0, //150
  0,   0,   0,   0,   0,   0,   0,   0,   0,   0, //160
  0,   0,   0,   0,   0,   0,   0,   0,   0,   0, //170
  0,   0,   0,   0,   0,   0,   0,   0,   0,   0, //180
  0,   0,   0,   0,   0,   0,   0,   0,   0,   0, //190
  0,   0,   0,   0,   0,   0,   0,   0,   0,   0, //200
  0,   0,   0,   0,   0,   0,   0,   0,   0,   0, //210
  0,   0,   0,   0,   0,   0,   0,   0,   0,   0, //220
  0,   0,   0,   0,   0,   0,   0,   0,   0,   0, //230
  0,   0,   0,   0,   0,   0,   0,   0,   0,   0, //240
  0,   0,   0,   0,   0,   0,   0,   0,   0,   0, //250
  0,   0,   0,   0,   0,   0,
}; // This array has 256 elements

// Converts binary data of length=len to base64 characters.
// Length of the resultant string is stored in flen
// (you must pass pointer flen).
char* base64( const void* binaryData, int len, int *flen )
{
  const unsigned char* bin = (const unsigned char*) binaryData ;
  char* res ;

  int rc = 0 ; // result counter
  int byteNo ; // I need this after the loop

  int modulusLen = len % 3 ;
  int pad = ((modulusLen&1)<<1) + ((modulusLen&2)>>1) ; // 2 gives 1 and 1 gives 2, but 0 gives 0.

  *flen = 4*(len + pad)/3 ;
  res = (char*) malloc( *flen + 1 ) ; // and one for the null
  if( !res )
  {
    puts( "ERROR: base64 could not allocate enough memory." ) ;
    puts( "I must stop because I could not get enough" ) ;
    return 0;
  }

  for( byteNo = 0 ; byteNo <= len-3 ; byteNo+=3 )
  {
    unsigned char BYTE0=bin[byteNo];
    unsigned char BYTE1=bin[byteNo+1];
    unsigned char BYTE2=bin[byteNo+2];
    res[rc++]  = b64[ BYTE0 >> 2 ] ;
    res[rc++]  = b64[ ((0x3&BYTE0)<<4) + (BYTE1 >> 4) ] ;
    res[rc++]  = b64[ ((0x0f&BYTE1)<<2) + (BYTE2>>6) ] ;
    res[rc++]  = b64[ 0x3f&BYTE2 ] ;
  }

  if( pad==2 )
  {
    res[rc++] = b64[ bin[byteNo] >> 2 ] ;
    res[rc++] = b64[ (0x3&bin[byteNo])<<4 ] ;
    res[rc++] = '=';
    res[rc++] = '=';
  }
  else if( pad==1 )
  {
    res[rc++]  = b64[ bin[byteNo] >> 2 ] ;
    res[rc++]  = b64[ ((0x3&bin[byteNo])<<4)   +   (bin[byteNo+1] >> 4) ] ;
    res[rc++]  = b64[ (0x0f&bin[byteNo+1])<<2 ] ;
    res[rc++] = '=';
  }

  res[rc]=0; // NULL TERMINATOR! ;)
  return res ;
}

unsigned char* unbase64( const char* ascii, int len, int *flen )
{
  const unsigned char *safeAsciiPtr = (const unsigned char*)ascii ;
  unsigned char *bin ;
  int cb=0;
  int charNo;
  int pad = 0 ;

  if( len < 2 ) { // 2 accesses below would be OOB.
    // catch empty string, return NULL as result.
    puts( "ERROR: You passed an invalid base64 string (too short). You get NULL back." ) ;
    *flen=0;
    return 0 ;
  }
  if( safeAsciiPtr[ len-1 ]=='=' )  ++pad ;
  if( safeAsciiPtr[ len-2 ]=='=' )  ++pad ;

  *flen = 3*len/4 - pad ;
  bin = (unsigned char*)malloc( *flen ) ;
  if( !bin )
  {
    puts( "ERROR: unbase64 could not allocate enough memory." ) ;
    puts( "I must stop because I could not get enough" ) ;
    return 0;
  }

  for( charNo=0; charNo <= len - 4 - pad ; charNo+=4 )
  {
    int A=unb64[safeAsciiPtr[charNo]];
    int B=unb64[safeAsciiPtr[charNo+1]];
    int C=unb64[safeAsciiPtr[charNo+2]];
    int D=unb64[safeAsciiPtr[charNo+3]];

    bin[cb++] = (A<<2) | (B>>4) ;
    bin[cb++] = (B<<4) | (C>>2) ;
    bin[cb++] = (C<<6) | (D) ;
  }

  if( pad==1 )
  {
    int A=unb64[safeAsciiPtr[charNo]];
    int B=unb64[safeAsciiPtr[charNo+1]];
    int C=unb64[safeAsciiPtr[charNo+2]];

    bin[cb++] = (A<<2) | (B>>4) ;
    bin[cb++] = (B<<4) | (C>>2) ;
  }
  else if( pad==2 )
  {
    int A=unb64[safeAsciiPtr[charNo]];
    int B=unb64[safeAsciiPtr[charNo+1]];

    bin[cb++] = (A<<2) | (B>>4) ;
  }

  return bin ;
}

RSA* loadPublicKeyFromString(const unsigned char* publicKeyStr)
{
    BIO *bio = BIO_new_mem_buf(static_cast<const void *>(publicKeyStr), -1);
    RSA* rsaPublicKey = PEM_read_bio_RSA_PUBKEY(bio, NULL, NULL, NULL) ;
    if (!rsaPublicKey) {
        LOGW << "ERROR: Could not load public KEY! PEM_read_bio_RSAPublicKey FAILED: " <<
                ERR_error_string(ERR_get_error(), NULL) << std::endl;
    }

    BIO_free(bio) ;
    return rsaPublicKey ;
}

unsigned char* rsaDecrypt(RSA *publicKey, const unsigned char* encryptedData, int dataLen, int *outLen)
{
    const int PADDING = RSA_PKCS1_PADDING;

    int rsaLen = RSA_size(publicKey) ; // That's how many bytes the decrypted data would be

    unsigned char *decryptedBin = (unsigned char*)malloc(dataLen);
    int encOffset = 0;
    for (;;) {
        if (encOffset >= dataLen) {
            break;
        }
        int result = RSA_public_decrypt(rsaLen, encryptedData + encOffset,
                                         decryptedBin + *outLen, publicKey, PADDING);
        if(result == -1) {
            LOGW << "ERROR: RSA_public_decrypt: " << ERR_error_string(ERR_get_error(), NULL) << std::endl;
            free(decryptedBin);
            *outLen = -1;
            return NULL;
        }
        *outLen += result;
        encOffset += rsaLen;
    }

    return decryptedBin;
}

unsigned char* rsaDecryptThisBase64(RSA *privKey, const char* base64String, int inLen, int *outLen)
{
  int encBinLen;
  unsigned char* encBin = unbase64(base64String, inLen, &encBinLen);

  // rsaDecrypt assumes length of encBin based on privKey
  unsigned char *decryptedBin = rsaDecrypt(privKey, encBin, encBinLen, outLen) ;
  free(encBin) ;

  return decryptedBin;
}

unsigned char *aesDecrypt(const unsigned char *key, const unsigned char *data, int dataLen, int *outLen)
{
    EVP_CIPHER_CTX *ctx = EVP_CIPHER_CTX_new();
    EVP_CIPHER_CTX_init(ctx);

    int ret = EVP_DecryptInit_ex(ctx, EVP_aes_128_ecb(), NULL, key, NULL);
    if (ret != 1) {
        LOGW << "EVP_DecryptUpdate failed!" << std::endl;
        EVP_CIPHER_CTX_free(ctx);
        return NULL;
    }

    unsigned char* result = new unsigned char[dataLen];

    int len1 = 0;
    ret = EVP_DecryptUpdate(ctx, result, &len1, data, dataLen);
    if (ret != 1) {
        LOGW << "EVP_DecryptUpdate failed!" << std::endl;
        EVP_CIPHER_CTX_free(ctx);
        delete result;
        return NULL;
    }

    int len2 = 0;
    ret = EVP_DecryptFinal_ex(ctx, result+len1, &len2);
    if (ret != 1) {
        LOGW << "EVP_DecryptFinal_ex failed!" << std::endl;
        EVP_CIPHER_CTX_free(ctx);
        delete result;
        return NULL;
    }

    ret = EVP_CIPHER_CTX_cleanup(ctx);
    if (ret != 1) {
        LOGW << "EVP_CIPHER_CTX_cleanup failed!";
        EVP_CIPHER_CTX_free(ctx);
        delete result;
        return NULL;
    }

    EVP_CIPHER_CTX_free(ctx);

    *outLen = len1 + len2;
    return result;
}

unsigned char *computeKeyFromGuardV1(const std::string &cipherBase64String,
                                      const std::string &guardBase64String,
                                      int *resultLen) {
    int keyLen;
    unsigned char* cipher = unbase64(cipherBase64String.c_str(), static_cast<int>(strlen(cipherBase64String.c_str())), &keyLen);

    int guardLen;
    unsigned char* guard = unbase64(guardBase64String.c_str(), static_cast<int>(strlen(guardBase64String.c_str())), &guardLen);

    unsigned char digest[SHA512_DIGEST_LENGTH];

    SHA512_CTX ctx;
    SHA512_Init(&ctx);
    SHA512_Update(&ctx, guard, guardLen);
    SHA512_Final(digest, &ctx);

    unsigned char guardKey[16];
    memcpy(guardKey, &digest[9], 4);
    memcpy(&guardKey[4], &digest[18], 4);
    memcpy(&guardKey[8], &digest[24], 4);
    memcpy(&guardKey[12], &digest[35], 4);

    unsigned char *decrypted = ::aesDecrypt(guardKey, cipher, keyLen, resultLen);
    free(cipher);
    free(guard);

    return decrypted;
}

DrmDecrypt instance;

}

DrmDecrypt::DrmDecrypt()
{

}

DrmDecrypt::~DrmDecrypt()
{

}

unsigned char *DrmDecrypt::rsaDecryptManifest(const unsigned char *key,
                                              const char *manifestBase64Cipher,
                                              int *resultLen,
                                              int *manifestVersion)
{
    RSA* publicKey = loadPublicKeyFromString(key);
    if (!publicKey) {
        return nullptr;
    }

    int len = 0;
    unsigned char *data = unbase64(manifestBase64Cipher, static_cast<int>(strlen(manifestBase64Cipher)), &len);

    uint32_t version = 0;
    memcpy(&version, &data[508], 4);
    *manifestVersion = ntohl(version);

    const int HEADER_LENGTH = 512;
    int manifestLen = len - HEADER_LENGTH;
    char *manifestCipher = reinterpret_cast<char *>(data + HEADER_LENGTH);
    unsigned char *result = rsaDecryptThisBase64(publicKey, manifestCipher, manifestLen, resultLen);

    free(data);
    RSA_free(publicKey);
    return result;
}

unsigned char *DrmDecrypt::aesDecrypt(const char *keyBase64String,
                                      const unsigned char *data,
                                      const int dataLen,
                                      int *resultLen)
{
    int keyLen = 0;
    unsigned char* key = unbase64(keyBase64String, static_cast<int>(strlen(keyBase64String)), &keyLen);

    unsigned char *decrypted = ::aesDecrypt(key, data, dataLen, resultLen);
    free(key);
    if (!decrypted) {
        return nullptr;
    }

    unsigned char *result = (unsigned char *)calloc((size_t)*resultLen, sizeof(unsigned char));
    if (!result) {
        free(decrypted);
        return nullptr;
    }
    memcpy(result, decrypted, (size_t)*resultLen);
    free(decrypted);

    return result;
}

DrmDecryptManager::DrmDecryptManager()
    : encrypted(false), drmVersion(0)
{

}

DrmDecryptManager::~DrmDecryptManager()
{

}

DrmDecryptManager &DrmDecryptManager::singleton()
{
    static DrmDecryptManager instance;
    return instance;
}

bool DrmDecryptManager::isEncrypted()
{
    return encrypted;
}

void DrmDecryptManager::setEncrypted(bool encrypted)
{
    this->encrypted = encrypted;
}

int DrmDecryptManager::getDrmVersion()
{
    return drmVersion;
}

int DrmDecryptManager::setDrmVersion(int version)
{
    drmVersion = version;
}

bool DrmDecryptManager::setAESKey(const std::string &aesKeyCipherBase64String, const std::string &aesKeyGuardBase64String)
{
    int keyLen;
    unsigned char *key = nullptr;
    if (drmVersion == 1) {
        key = computeKeyFromGuardV1(aesKeyCipherBase64String, aesKeyGuardBase64String, &keyLen);
    }
    if (!key) {
        return false;
    }

    int strLen;
    this->aesKeyBase64String = base64(key, keyLen, &strLen);
    free(key);
    return true;
}

unsigned char *DrmDecryptManager::aesDecrypt(const unsigned char *data, const int dataLen, int *resultLen)
{
    return drmDecrypt.aesDecrypt(aesKeyBase64String.c_str(), data, dataLen, resultLen);
}
