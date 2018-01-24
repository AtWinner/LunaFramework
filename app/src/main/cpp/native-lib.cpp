#include <jni.h>
#include <string>
#include "native-lib.h"

jstring Java_com_luna_lunaframework_MainActivity_stringFromJNI(JNIEnv *env, jobject instance) {
    std::string hello = "Hello from C++";

    char *string;
    getString((char **) &string);
    hello = string;
    return env->NewStringUTF(hello.c_str());
}

jstring Java_com_luna_lunaframework_MainActivity_string1FromJNI(JNIEnv *env, jobject instance) {
    std::string hello = "Hello from C++";

    char *string;
    getString((char **) &string);
    hello = string;
    return env->NewStringUTF(hello.c_str());
}

char *getString(char **string) {
    *string = "hello 你大爷";
    return *string;
}