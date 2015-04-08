#include<stdio.h>

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>           // close()
#include <string.h>           // strcpy, memset(), and memcpy()

#include <netdb.h>            // struct addrinfo
#include <sys/types.h>        // needed for socket(), uint8_t, uint16_t, uint32_t
#include <sys/socket.h>       // needed for socket()
#include <netinet/in.h>       // IPPROTO_RAW, IPPROTO_IP, IPPROTO_TCP, INET_ADDRSTRLEN
#include <netinet/ip.h>       // struct ip and IP_MAXPACKET (which is 65535)
#include <netinet/tcp.h>      // struct tcphdr
#include <arpa/inet.h>        // inet_pton() and inet_ntop()
#include <sys/ioctl.h>        // macro ioctl is defined
//#include <bits/ioctls.h>      // defines values for argument "request" of ioctl.
#include <net/if.h>           // struct ifreq

#include <errno.h>            // errno, perror()
#define IP4_HDRLEN 20         // IPv4 header length
#define TCP_HDRLEN 20         // TCP header length, excludes options data

#include "edu_stonybrook_middleboxes_TcpTests.h"
JNIEXPORT jstring Java_edu_stonybrook_middleboxes_TcpTests_tcpResetTest(JNIEnv *env, jobject obj)
  {
    return (env)->NewStringUTF("Hello Praveen");
    char* local_ip ;
    char *target_ip;
    struct in_addr src_ip;
    struct in_addr dest_ip;
    inet_aton(local_ip, &src_ip);
    inet_aton(target_ip, &dest_ip);
    struct ip iphdr;
    struct tcphdr tcph;
    int sd; // socket descriptor
    if ((sd = socket (AF_INET, SOCK_RAW, IPPROTO_RAW)) < 0) {
        perror ("socket() failed to get socket descriptor for using ioctl() ");
        exit (EXIT_FAILURE);
      }
    //fill the IP Header
    iphdr.ip_v = 4;
    iphdr.ip_len = IP4_HDRLEN;
    iphdr.ip_hl = 5;
    iphdr.ip_id = htons(0);
    iphdr.ip_p = IPPROTO_TCP;
    iphdr.ip_off = htons(0);
    iphdr.ip_dst = dest_ip;
    iphdr.ip_src = src_ip;
    iphdr.ip_tos = 16;
    iphdr.ip_sum = 0;
    //  fill the TCP header
    tcph.doff =
    tcph.source = htons(12000);
    tcph.dest = htons(8080);
    tcph.seq = htons(0);
    tcph.ack_seq = htons(0);
    tcph.syn = 1;
    tcph.ack = 0;
    tcph.rst = 0;
    tcph.psh = 0;
    tcph.fin = 0;
    tcph.urg = 0;
    tcph.ece = 0;
    tcph.cwr = 0;
    tcph.window = htons(10000);
    tcph.urg_ptr= htons(0);
    tcph.check =0;
    const int on = 1;
    if (setsockopt (sd, IPPROTO_IP, IP_HDRINCL, &on, sizeof (on)) < 0) {
        perror ("setsockopt() failed to set IP_HDRINCL ");
        exit (EXIT_FAILURE);
      }

  }
