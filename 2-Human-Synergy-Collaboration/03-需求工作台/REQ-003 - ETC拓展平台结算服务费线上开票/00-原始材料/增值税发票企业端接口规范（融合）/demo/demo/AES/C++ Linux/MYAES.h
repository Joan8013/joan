#ifndef MY_AES_H
#define MY_AES_H

#include <string>

using namespace std;

char* md5_string(char* message);
char* base64_encode(unsigned char* message, int nsize);
unsigned char* base64_decode(char* message, int *psize);
char* aes_encode(char* message, char* password);
char* aes_decode(char* message, char* password);
void freepointer(char* pstr);

#endif
