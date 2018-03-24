/*--------------------------------------------------------------------------
File: mon.c

Description:  This program creates a process with the program name provided
              on the command line. It then starts procmon to monitor
              the first process. After 20 seconds it sends signals
              to the processes to terminate them.
--------------------------------------------------------------------------*/
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <signal.h>


/* Its all done in the main function */
int main(int argc, char **argv)
{
    char    *program;
    char    pidStr[32];
    int     prpid, monpid;

    if (argc != 2) {
        printf("Usage: mon fileName\n where fileName is an executable file.\n");
        exit(-1);
    } else
        program = argv[1];  /* This is the program to monitor */

    prpid = fork();
    if (prpid == -1)  /* Error */
    {
        printf("First fork failed.\n");
        exit(-1);
    } 
    else if (prpid == 0)  /* Child - let's start the program */
    {
        execl(program, program, NULL);
	exit(-1); /* in case execl fails */
    }
    

    /* In the parent */
    sprintf(pidStr, "%d", prpid);  /* Convert the pid number to a character string */
    monpid = fork();   /* Another child process to run procmon */
    if (monpid == -1) /* Error */
    {
        printf("Second fork failed.\n");
        kill(prpid, SIGTERM); /* Kills the other child */
        exit(-1);
    } 
    else if (monpid == 0)  /* In the child process start up the procmon program */
        execl("procmon", "procmon", pidStr, NULL);
    
    /* Here we are still in the parent */
   
    sleep(20);	/* Wait 20 seconds */
    printf("Killing %s\n", program); 
    kill(prpid, SIGTERM); /* Finished with the first child */
    sleep(2);
    printf("Killing procmon.\n"); /* Finished with procmon */
    kill(monpid, SIGTERM);

    return(0);
}
