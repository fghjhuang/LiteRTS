#include <jni.h>
#include <string.h>
#include <android/log.h>

#ifdef HAVE_CONFIG_H
#include "config.h"
#endif

#include <stdio.h>
#include <stdlib.h>
#include <limits.h>
#include <stdint.h>
#include <math.h>
#include <string.h>
#include <time.h>
#if (!defined WIN32 && !defined _WIN32) || defined(__MINGW32__)
#include <unistd.h>
#else
#include <process.h>
#define getpid _getpid
#endif
#include "opus_multistream.h"
#include "opus.h"
#include "../src/opus_private.h"

#define MAX_PACKET (1500)
#define SAMPLES (48000*30)
#define SSAMPLES (SAMPLES/3)
#define MAX_FRAME_SAMP (5760)
//每采集一帧大小为160short=320个字节
//帧的计算，例如 采样率8000 单通道 采样位数16bit
//1秒采集到数据8000X1X16=16000Byte
//网络语音传输:20ms一次 也就是说1秒发50次320Byte的包 320Byte=160Short
//采集一次是160Short
#define FRAME_SIZE 160
#define SAMPLE_RATE 8000
#define CHANNELS 1
#define APPLICATION OPUS_APPLICATION_AUDIO
//参数调整压缩倍数可设置7000 声音还能正常,压缩率160short 压缩成18short
#define BITRATE 8000

#define MAX_FRAME_SIZE 6*960
#define MAX_PACKET_SIZE (3*1276)
OpusEncoder *encoder;
OpusDecoder *decoder;

int bandwidth = OPUS_AUTO;
int use_vbr = 0;
int cvbr=1;
int complexity = 4;
int packet_loss_perc=0;


jstring Java_com_huangstudio_audiolibrary_OpusHelper_test(JNIEnv *env,jobject object){
   return (*env)->NewStringUTF(env,"testopus2");
}
//初始化语音
//com_gauss_ opus _encode java层的包名，Opus类名，open函数名 这样定义好java就可以直接调用了
jint Java_com_huangstudio_audiolibrary_OpusHelper_open(JNIEnv *env,jobject thiz,jint compression){

   int err;
    encoder = opus_encoder_create(SAMPLE_RATE, CHANNELS, APPLICATION, &err);
   if (err<0)
   {

      return 0;
   }

   err = opus_encoder_ctl(encoder, OPUS_SET_BITRATE(BITRATE));
   if (err<0)
   {
      return 0;
   }

   opus_encoder_ctl(encoder, OPUS_SET_BANDWIDTH(bandwidth));
   opus_encoder_ctl(encoder, OPUS_SET_VBR(use_vbr));
   opus_encoder_ctl(encoder, OPUS_SET_VBR_CONSTRAINT(cvbr));
   opus_encoder_ctl(encoder, OPUS_SET_COMPLEXITY(compression));
   opus_encoder_ctl(encoder,   OPUS_SET_PACKET_LOSS_PERC(packet_loss_perc));
   decoder = opus_decoder_create(SAMPLE_RATE, CHANNELS, &err);
   if (err<0)
   {
      return 0;
   }
   return 1;

}
JNIEXPORT
jint Java_com_huangstudio_audiolibrary_OpusHelper_getFrameSize(JNIEnv *env,jobject thiz)
{
    return  FRAME_SIZE;
}
//压缩
JNIEXPORT
jint Java_com_huangstudio_audiolibrary_OpusHelper_encode(JNIEnv *env,jobject thiz,jshortArray lin, jint offset, jbyteArray encoded, jint size)
{
    int err;
    int i;
    int nbBytes;
    unsigned short cbits[480];
    unsigned short out[160];

    if(encoder == NULL){
        return 0;
    }
  //读取参数 数组
    (*env)->GetShortArrayRegion(env,lin,offset,size,out);
nbBytes = opus_encode(encoder,out , size, cbits, 480);//
//设定压缩完的数据到数组中
    (*env)->SetByteArrayRegion(env,encoded, 0, nbBytes, (jshort*)cbits);

    return nbBytes;

}
//解压
JNIEXPORT
jint Java_com_huangstudio_audiolibrary_OpusHelper_decode(JNIEnv *env,jobject thiz,
                                                       jbyteArray encoded, jshortArray lin, int size)
{
    int err;
    int i;
    int frame_size;

    int nbBytes;
    unsigned char cbits[MAX_PACKET_SIZE];
    unsigned short out[160];

    unsigned short deout[160];
    if(decoder == NULL){
        return 0;
    }

    (*env)->GetByteArrayRegion(env, encoded,0,size,out);
    frame_size = opus_decode(decoder, out, size, deout, MAX_FRAME_SIZE, 0);

    if(frame_size <0){
        return 0;
    }

    int nSize = frame_size*CHANNELS;
    (*env)->SetShortArrayRegion(env,lin, 0, nSize, (jshort*)deout);

    return nSize;

}


//释放语音对象

void Java_com_huangstudio_audiolibrary_OpusHelper_close(JNIEnv *env,jobject thiz)
{

  if(encoder !=NULL){
   opus_encoder_destroy(encoder);
  }
 if(encoder !=NULL){
  opus_decoder_destroy(decoder);
  }
}