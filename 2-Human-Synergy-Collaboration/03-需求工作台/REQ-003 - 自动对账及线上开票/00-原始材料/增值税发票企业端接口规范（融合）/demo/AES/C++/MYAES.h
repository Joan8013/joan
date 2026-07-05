#ifndef MY_AES_H
#define MY_AES_H

#include <string>

using namespace std;

extern "C" __declspec(dllimport) char* md5_string(char* message);
extern "C" __declspec(dllimport) char* base64_encode(unsigned char* message, int nsize);
extern "C" __declspec(dllimport) unsigned char* base64_decode(char* message, int *psize);
extern "C" __declspec(dllimport) char* aes_encode(char* message, char* password);
extern "C" __declspec(dllimport) char* aes_decode(char* message, char* password);
extern "C" __declspec(dllimport) void freepointer(char* pstr);

/*
typedef string(*p_md5_string)(const string & message);
typedef string(*p_base64_encode)(const string & message);
typedef string(*p_base64_decode)(const string & message);
typedef string(*p_aes_encode)(const string & message, const string &password);
typedef string(*p_aes_decode)(const string & message, const string &password);
*/

#endif