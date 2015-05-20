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
#include <netinet/tcp.h>
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

uint16_t
tcp4_checksum (struct ip iphdr, struct tcphdr tcphdr);
uint16_t
checksum (uint16_t *addr, int len);
int find_max(int a, int b, int c)
{
	int max = a;
	if(b >max)
		max = b;
	if(c > max)
		max = c;
	return max;
}

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
    struct in_addr src_ip;
    struct in_addr dest_ip;
    inet_aton(local_ip, &src_ip);
    inet_aton(target_ip, &dest_ip);
    struct sockaddr_in destaddr;
    struct sockaddr_in cliaddr;
    int socklen = sizeof(struct sockaddr_in);
    struct ip iphdr;
    struct ip *rcviphdr;
    struct tcphdr tcph;
    struct udphdr udph;
    char buffer[512];
    char recvbuffer[1024];
    struct timeval timeout;
    int ready_to_read;
    int sd; // socket descriptor
    int icmp_sd;
    int tcp_sd;
    int max_iters=20;
    int iteration = 1;
    int maxfd;
    fd_set descset;
    FD_ZERO(&descset);
    if ((sd = socket (PF_INET, SOCK_RAW, IPPROTO_RAW)) < 0) {
        perror ("socket() failed to get socket descriptor for using ioctl() ");
        return -1;
        //__android_log_print(ANDROID_LOG_VERBOSE,"INFO", strerror(errno),1);
        //return (env)->NewStringUTF("socket create fail");

      }
    icmp_sd = socket(PF_INET, SOCK_RAW, IPPROTO_ICMP);
    tcp_sd = socket(PF_INET, SOCK_RAW, IPPROTO_TCP);
    bzero(&destaddr, sizeof(struct sockaddr_in));
    destaddr.sin_family = AF_INET;
    destaddr.sin_port = htons(8080);
    destaddr.sin_addr = dest_ip;
    //fill the IP Header
    while(iteration < max_iters)
    {
    	int tcp_seqnumber=0;
    	int middlebox_ttl=0;
    		ready_to_read = 0;
    		iphdr.ip_v = 4;
    	    iphdr.ip_len = IP4_HDRLEN+ TCP_HDRLEN;
    	    iphdr.ip_hl = 5;
    	    iphdr.ip_id = htons(0);
    	    iphdr.ip_p = IPPROTO_TCP;
    	    iphdr.ip_off = htons(0);
    	    iphdr.ip_dst = dest_ip;
    	    iphdr.ip_src = src_ip;
    	    iphdr.ip_tos = 16;
    	    iphdr.ip_sum = 0;
    	    iphdr.ip_ttl = iteration;
	    // fill UDP header
	    /*udph.source = htons(destPort);
	    udph.dest = htons(destPort);
	    udph.len = htons(UDP_HDRLEN+ strlen(data));
	    printf(" udph len =%d\n", udph.len);
	    //udph.check=udp4_checksum(iphdr, udph, data, strlen(data));
	    udph.check=0;*/
    	    //  fill the TCP header
    	    tcph.doff = 5;
    	    tcph.source = htons(12000);
    	    tcph.dest = htons(destPort);
    	    tcph.seq = htons(0);
    	    tcp_seqnumber = tcph.seq;
    	    tcph.ack_seq = htons(0);
	        //tcph.th_x2 = 0;
    	    tcph.res1 = 0;
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
    	    tcph.check =tcp4_checksum(iphdr,tcph);
    	    //tcph.check = htons(27887);
	    //tcph.check = 0;
    	    const int on = 1;
    	    if (setsockopt (sd, IPPROTO_IP, IP_HDRINCL, &on, sizeof (on)) < 0) {
    	        perror ("setsockopt() failed to set IP_HDRINCL ");
    	        return -1;
    	      }

    	    memcpy(buffer, (void *)&iphdr, sizeof(iphdr));
    	    memcpy(buffer+sizeof(iphdr), (void *)&tcph, sizeof(tcph));
    	    //memcpy((buffer+IP4_HDRLEN+TCP_HDRLEN), (void*)data, strlen(data));
    	    //printf("%s", buffer);
    	    if(sendto(sd,buffer, IP4_HDRLEN+TCP_HDRLEN, 0,(struct sockaddr*)&destaddr, sizeof(struct sockaddr_in)) <0)
    	    {
    	    	printf("error in sending %s\n",strerror(errno));
    	    	return -1;
    	    }
    	    timeout.tv_sec = 3;
    	    timeout.tv_usec = 0;

    	    FD_SET(sd, &descset);
	    FD_SET(icmp_sd, &descset);
	    FD_SET(tcp_sd, &descset);
	    maxfd = find_max(sd, icmp_sd, tcp_sd);
    	    ready_to_read = select(maxfd+1, &descset, NULL, NULL, &timeout);
    	    if(ready_to_read)
    	    {
	    	//printf("something available\n");
    	    	if(FD_ISSET(tcp_sd,&descset))
    	    	{
    	    		//printf("reached server\n");
			int tcprcvd = recvfrom(tcp_sd, recvbuffer, 1024, 0, (struct sockaddr*)&cliaddr, &socklen);
			struct ip *riph = (struct ip *)(recvbuffer);
			char *srcip = inet_ntoa(riph->ip_src);
			//printf("tcp sourec ip =%s targetip %s", srcip,target_ip);
			if(!strcmp(target_ip, srcip))
			{
				printf(" reached target server at ttl = %d\n",iteration);
				return 0;
			}

    	    	}
    	    	if(FD_ISSET(icmp_sd, &descset))
    	    	{
					int rcvd = recvfrom(icmp_sd,recvbuffer, 1024, 0, (struct sockaddr*)&cliaddr,  &socklen);
					rcviphdr = (struct ip *) (recvbuffer);
					//printf(" source ip = %s",inet_ntoa(rcviphdr->ip_src));
					//printf("dest ip = %s",inet_ntoa(rcviphdr->ip_dst));
					//printf(" got something\n");
					struct icmp *icmph = (struct icmp*)(recvbuffer+IP4_HDRLEN);
					struct ip icmpiphdr = icmph->icmp_ip;
					struct tcphdr *icmptcph  = (struct tcphdr*)(recvbuffer+IP4_HDRLEN+8+IP4_HDRLEN);

					//printf(" icmp ip options %d\n",icmpiphdr.ip_ttl);
					//printf(" Tcp seq number = %d",icmptcph->seq);
					int reply_tcpseqnumber = icmptcph->seq;
					int icmp_src_port = ntohl(icmptcph->source);
					if(reply_tcpseqnumber != tcp_seqnumber || icmp_src_port != 12000)
					{
						middlebox_ttl = iteration;
						printf("Middlebox could be before hop count %d\n",middlebox_ttl);
						return 0;
					}

					//printf("icmp code = %d type =%d\n", icmph->icmp_code, icmph->icmp_type);
    	    	}
    	    }
    	    else
    	    {
    	    	//printf(" timeout occured\n");
    	    }
    	    iteration++;
    }
    printf(" timed out\n");
    return 0;

  }
// Build IPv4 TCP pseudo-header and call checksum function.
uint16_t
tcp4_checksum (struct ip iphdr, struct tcphdr tcphdr)
{
  uint16_t svalue;
  char buf[IP_MAXPACKET], cvalue;
  char *ptr;
  int chksumlen = 0;

  // ptr points to beginning of buffer buf
  ptr = &buf[0];

  // Copy source IP address into buf (32 bits)
  memcpy (ptr, &iphdr.ip_src.s_addr, sizeof (iphdr.ip_src.s_addr));
  ptr += sizeof (iphdr.ip_src.s_addr);
  chksumlen += sizeof (iphdr.ip_src.s_addr);

  // Copy destination IP address into buf (32 bits)
  memcpy (ptr, &iphdr.ip_dst.s_addr, sizeof (iphdr.ip_dst.s_addr));
  ptr += sizeof (iphdr.ip_dst.s_addr);
  chksumlen += sizeof (iphdr.ip_dst.s_addr);

  // Copy zero field to buf (8 bits)
  *ptr = 0; ptr++;
  chksumlen += 1;

  // Copy transport layer protocol to buf (8 bits)
  memcpy (ptr, &iphdr.ip_p, sizeof (iphdr.ip_p));
  ptr += sizeof (iphdr.ip_p);
  chksumlen += sizeof (iphdr.ip_p);

  // Copy TCP length to buf (16 bits)
  svalue = htons (sizeof (tcphdr));
  memcpy (ptr, &svalue, sizeof (svalue));
  ptr += sizeof (svalue);
  chksumlen += sizeof (svalue);

  // Copy TCP source port to buf (16 bits)
  memcpy (ptr, &tcphdr.source, sizeof (tcphdr.source));
  ptr += sizeof (tcphdr.source);
  chksumlen += sizeof (tcphdr.source);

  // Copy TCP destination port to buf (16 bits)
  memcpy (ptr, &tcphdr.dest, sizeof (tcphdr.dest));
  ptr += sizeof (tcphdr.dest);
  chksumlen += sizeof (tcphdr.dest);

  // Copy sequence number to buf (32 bits)
  memcpy (ptr, &tcphdr.seq, sizeof (tcphdr.seq));
  ptr += sizeof (tcphdr.seq);
  chksumlen += sizeof (tcphdr.seq);

  // Copy acknowledgement number to buf (32 bits)
  memcpy (ptr, &tcphdr.ack_seq, sizeof (tcphdr.ack_seq));
  ptr += sizeof (tcphdr.ack_seq);
  chksumlen += sizeof (tcphdr.ack_seq);

  // Copy data offset to buf (4 bits) and
  // copy reserved bits to buf (4 bits)
  cvalue = (tcphdr.doff << 4) + tcphdr.res1;
  memcpy (ptr, &cvalue, sizeof (cvalue));
  ptr += sizeof (cvalue);
  chksumlen += sizeof (cvalue);

    u_char th_flags;
    int tcp_flags[8];

    // FIN flag (1 bit)
    tcp_flags[0] = 0;

    // SYN flag (1 bit): set to 1
    tcp_flags[1] = 1;

    // RST flag (1 bit)
    tcp_flags[2] = 0;

    // PSH flag (1 bit)
    tcp_flags[3] = 0;

    // ACK flag (1 bit)
    tcp_flags[4] = 0;

    // URG flag (1 bit)
    tcp_flags[5] = 0;

    // ECE flag (1 bit)
    tcp_flags[6] = 0;

    // CWR flag (1 bit)
    tcp_flags[7] = 0;

    th_flags = 0;
    int i;
    for (i=0; i<8; i++) {
      th_flags += (tcp_flags[i] << i);
    }


    // Copy TCP flags to buf (8 bits)
    memcpy (ptr, &th_flags, sizeof (th_flags));
    ptr += sizeof (th_flags);
    chksumlen += sizeof (th_flags);

  // Copy TCP window size to buf (16 bits)
  memcpy (ptr, &tcphdr.window, sizeof (tcphdr.window));
  ptr += sizeof (tcphdr.window);
  chksumlen += sizeof (tcphdr.window);

  // Copy TCP checksum to buf (16 bits)
  // Zero, since we don't know it yet
  *ptr = 0; ptr++;
  *ptr = 0; ptr++;
  chksumlen += 2;

  // Copy urgent pointer to buf (16 bits)
  memcpy (ptr, &tcphdr.urg_ptr, sizeof (tcphdr.urg_ptr));
  ptr += sizeof (tcphdr.urg_ptr);
  chksumlen += sizeof (tcphdr.urg_ptr);

  return checksum ((uint16_t *) buf, chksumlen);
}
uint16_t
checksum (uint16_t *addr, int len)
{
  int count = len;
  register uint32_t sum = 0;
  uint16_t answer = 0;

  // Sum up 2-byte values until none or only one byte left.
  while (count > 1) {
    sum += *(addr++);
    count -= 2;
  }

  // Add left-over byte, if any.
  if (count > 0) {
    sum += *(uint8_t *) addr;
  }

  // Fold 32-bit sum into 16 bits; we lose information by doing this,
  // increasing the chances of a collision.
  // sum = (lower 16 bits) + (upper 16 bits shifted right 16 bits)
  while (sum >> 16) {
    sum = (sum & 0xffff) + (sum >> 16);
  }

  // Checksum is one's compliment of sum.
  answer = ~sum;

  return (answer);
}
