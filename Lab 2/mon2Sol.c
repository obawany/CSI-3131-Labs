/*--------------------------------------------------------------------------
File: mon2.c

Description: This program creates a process to run the program identified
             on the commande line.  It will then start procmon in another
	     process to monitor the change in the state of the first process.
	     After 20 seconds, signals are sent to the processes to terminate
	     them.

	     Also a third process is created to run the program filter.  
	     A pipe is created between the procmon process and the filter
	     process so that the standard output from procmon is sent to
	     the standard input of the filter process.
--------------------------------------------------------------------------*/
#include <stdio.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <signal.h>
#include <string.h>


/* It's all done in main*/
int main(int argc, char **argv)
{
    char    *program;
    char    pidStr[32];
    int     prpid, monpid, fpid;
    int     pipefd[2];

    if (argc != 2) {
        printf("Usage: mon fileName\n where fileName is an executable file.\n");
        exit(-1);
    } else
        program = argv[1];  /* This is the program to monitor */


    /* First Step: Create the first process to run the program from the command line */
    prpid = fork();
    if (prpid == -1)  /* Error */
    {
        printf("First fork failed.\n");
        exit(-1);
    } 
    else if (prpid == 0)  /* Child - start the program */
    {
        execl(program, program, NULL);
	exit(-1); /* in case execl fails */
    }

    /* Second step: Create the pipe to be used for connecting procmon to filter */
    if (pipe(pipefd) == -1) 
    {
       printf("Pipe creation failed.\n");  /* Error */
       exit(-1);
    }

    /* Third step: Lets create the filter process - don't forget to connect to the pipe */
    fpid = fork(); /* for the process filter */
    if (fpid == -1) 
    {  /* Error */
            printf("Second fork failed.\n"); 
            kill(prpid, SIGTERM); /* stop the first process */
            exit(-1);
    } 
    else if (fpid == 0) 
    { /* In the child process to run filter */
            /* redirect the standard input to the pipe */
            if(dup2(pipefd[0], 0) == -1) { perror("dup2: "); exit(-1); }
	    close(pipefd[0]); /* no longer need these file descriptors */
            close(pipefd[1]);
            execlp("filter", "filter", NULL);
            exit(-1);  /* in case filter fails */
     } 

    /* Fourth step: Lets create the procmon process - don't forget to connect to the pipe */
     sprintf(pidStr, "%d", prpid);
     monpid = fork(); /* forking for procmon */
     if (monpid == -1) 
     {   /* Erreur */
         printf("Third fork failed.\n");
         kill(fpid, SIGTERM); /* kill the filter program */
         kill(prpid, SIGTERM); /* kill the monitored program */
         exit(-1);
     } 
     else if (monpid == 0) 
     { /* start procmon but first redirect the standard output to the pipe */
         dup2(pipefd[1], 1);
	 close(pipefd[0]);
         close(pipefd[1]);
         execlp("procmon", "procmon", pidStr, NULL);
         exit(-1);  /* in case procmon fails */
     }

    /* No need to communicate with the pipe */
    close(pipefd[0]);
    close(pipefd[1]);

    /* Fifth step: Let things run for 20 seconds */
    sleep(20);

    /* Last step: 
       1. Kill the process running the program
       2. Sleep for 2 seconds 
       3. Kill the procmon and filter processes
    */
    printf("Killing %s\n", program);
    kill(prpid, SIGTERM);
    sleep(2);
    printf("Killing procmon.\n");
    kill(monpid, SIGTERM);
    printf("Killing filter.\n");
    kill(fpid, SIGTERM);

    return(0);
}



