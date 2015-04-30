#include<stdio.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>           // close()
#include <string.h>           // strcpy, memset(), and memcpy()
#include <sys/select.h>		  // select()
#include <netdb.h>            // struct addrinfo
#include <sys/types.h>        // needed for socket(), uint8_t, uint16_t, uint32_t
#include <sys/socket.h>       // needed for socket()
#include <sys/time.h>
#include <netinet/in.h>       // IPPROTO_RAW, IPPROTO_IP, IPPROTO_TCP, INET_ADDRSTRLEN
#include <netinet/ip.h>       // struct ip and IP_MAXPACKET (which is 65535)
#include <netinet/udp.h>      // struct udphdr
#include <netinet/ip_icmp.h>  // struct icmphdr
#include <arpa/inet.h>        // inet_pton() and inet_ntop()
#include <sys/ioctl.h>        // macro ioctl is defined
//#include <bits/ioctls.h>      // defines values for argument "request" of ioctl.
#include <net/if.h>           // struct ifreq

#include <errno.h>            // errno, perror()
#define IP4_HDRLEN 20         // IPv4 header length
#define TCP_HDRLEN 20         // TCP header length, excludes options data
#define UDP_HDRLEN 8
#define ETH_HDRLEN 14  // Ethernet header length
#define ICMP_HDRLEN 8  // ICMP header length for echo request, excludes data


//#include "edu_stonybrook_middleboxes_TcpTests.h"
/*JNIEXPORT jstring JNICALL Java_edu_stonybrook_middleboxes_TcpTests_tcpResetTest
  (JNIEnv *env, jobject obj, jstring localIp, jstring serverIp, jint port)*/
int main(int argc, char **argv)
  {
	//return (env)->NewStringUTF("Hello Praveen");
	if( argc < 3)
	{
		return -1;
	}
    const char *local_ip=  argv[1];
    const char *target_ip = argv[2];
    int destPort = atoi(argv[3]);
    const char *data ="Hello praveen";
    printf(" string legnth =%d\n", strlen(data));
    struct in_addr src_ip;
    struct in_addr dest_ip;
    inet_aton(local_ip, &src_ip);
    inet_aton(target_ip, &dest_ip);
    struct sockaddr_in destaddr;
    struct sockaddr_in cliaddr;
    int socklen = sizeof(struct sockaddr_in);
    struct ip iphdr;
    struct ip *rcviphdr;
    struct udphdr udph;
    char buffer[512];
    char recvbuffer[1024];
    struct timeval timeout;
    int ready_to_read;
    int sd; // socket descriptor
    int icmp_sd;
    int max_iters=10;
    int iteration = 0;
    int maxfd;
    fd_set descset;
    FD_ZERO(&descset);
    if ((sd = socket (PF_INET, SOCK_RAW, IPPROTO_RAW)) < 0) {
        perror ("socket() failed to get socket descriptor for using ioctl() ");
        //__android_log_print(ANDROID_LOG_VERBOSE,"INFO", strerror(errno),1);
        //return (env)->NewStringUTF("socket create fail");

      }
    icmp_sd = socket(PF_INET, SOCK_RAW, IPPROTO_ICMP);
    bzero(&destaddr, sizeof(struct sockaddr_in));
    destaddr.sin_family = AF_INET;
    destaddr.sin_port = htons(8080);
    destaddr.sin_addr = dest_ip;
    //fill the IP Header
    while(iteration < max_iters)
    {
    	printf(" iteration %d\n",iteration);
    		ready_to_read = 0;
    		iphdr.ip_v = 4;
    	    iphdr.ip_len = IP4_HDRLEN+ UDP_HDRLEN;
    	    iphdr.ip_hl = 5;
    	    iphdr.ip_id = htons(0);
    	    iphdr.ip_p = IPPROTO_UDP;
    	    iphdr.ip_off = htons(0);
    	    iphdr.ip_dst = dest_ip;
    	    iphdr.ip_src = src_ip;
    	    iphdr.ip_tos = 16;
    	    iphdr.ip_sum = 0;
    	    iphdr.ip_ttl = iteration;
	    // fill UDP header
	    udph.source = htons(destPort);
	    udph.dest = htons(destPort);
	    udph.len = htons(UDP_HDRLEN+ strlen(data));
	    printf(" udph len =%d\n", udph.len);
	    //udph.check=udp4_checksum(iphdr, udph, data, strlen(data));
	    udph.check=0;
    	    /*//  fill the TCP header
    	    tcph.doff = 5;
    	    tcph.source = htons(12000);
    	    tcph.dest = htons(destPort);
    	    tcph.seq = htons(0);
    	    tcph.ack_seq = htons(0);
    	    tcph.syn = 1;
    	    tcph.ack = 0;
    	    tcph.rst = 0;
    	    tcph.psh = 0;
    	    tcph.fin = 0;
    	    tcph.urg = 0;
    	    //tcph.ece = 0;
    	    //tcph.cwr = 0;
    	    tcph.window = htons(10000);
    	    tcph.urg_ptr= htons(0);
    	    tcph.check =0;*/
    	    const int on = 1;
    	    if (setsockopt (sd, IPPROTO_IP, IP_HDRINCL, &on, sizeof (on)) < 0) {
    	        perror ("setsockopt() failed to set IP_HDRINCL ");
    	        exit (EXIT_FAILURE);
    	      }

    	    memcpy(buffer, (void *)&iphdr, sizeof(iphdr));
    	    memcpy(buffer+sizeof(iphdr), (void *)&udph, sizeof(udph));
    	    memcpy((buffer+IP4_HDRLEN+UDP_HDRLEN), (void*)data, strlen(data));
    	    printf("%s", buffer);
    	    if(sendto(sd,buffer, IP4_HDRLEN+UDP_HDRLEN+strlen(data), 0,(struct sockaddr*)&destaddr, sizeof(struct sockaddr_in)) <0)
    	    	printf("error in sendining %s\n",strerror(errno));
    	    timeout.tv_sec = 3;
    	    timeout.tv_usec = 0;

    	    FD_SET(sd, &descset);
	    FD_SET(icmp_sd, &descset);
	    if(icmp_sd>  sd)
	    	maxfd = icmp_sd;
	    else
	    	maxfd = sd;
    	    ready_to_read = select(maxfd+1, &descset, NULL, NULL, &timeout);
    	    if(ready_to_read)
    	    {
    	    	int rcvd = recvfrom(icmp_sd,recvbuffer, 1024, 0, (struct sockaddr*)&cliaddr,  &socklen);
    	    	rcviphdr = (struct ip *) (recvbuffer);
    	    	printf(" source ip = %s",inet_ntoa(rcviphdr->ip_src));
    	    	printf("dest ip = %s",inet_ntoa(rcviphdr->ip_dst));
    	    	printf(" got something\n");

    	    }
    	    else
    	    {
    	    	printf(" timeout occured\n");
    	    }
    	    iteration++;
    }
    return 0;

  }
