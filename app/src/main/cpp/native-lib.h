//
// Created by huhu on 2018/1/15.
//

#ifndef LUNAFRAMEWORK_NATIVE_LIB_H
#define LUNAFRAMEWORK_NATIVE_LIB_H

#endif //JNIPRACTICE_NATIVE_LIB_H_H
extern "C"
jstring Java_com_luna_lunaframework_MainActivity_stringFromJNI(JNIEnv *, jobject);

jstring Java_com_luna_lunaframework_MainActivity_string1FromJNI(JNIEnv *, jobject);

char *getString(char **);
