#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <signal.h>


/* the program execution starts here */
int main(int argc, char **argv)
{
    char    *program;

    if (argc != 2) {
        printf("Usage: mon fileName\n where fileName is an executable file.\n");
        exit(-1);
    } else
        program = argv[1];

    /* Here comes your code.*/
    /* It should do the following:
        1. launch the program specified by the variable 'fileName' and get its pid
        2. launch 'procmon pid' where pid is the pid of the program launched in step 1
        3. wait for 20 seconds
        4. kill the first program
        5. wait for 2 seconds
        6. kill the procmon
    */
}
