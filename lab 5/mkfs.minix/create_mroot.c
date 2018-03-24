/*-----------------------------------------------------------------
File: create_mroot.c
Description: Contains the code for creating a minix root directory.
------------------------------------------------------------------*/

#include "mkfs.minix.h"

/*-----------------------------------------------------------------
Routine: create_mroot()

Arguments: int mfd;  - fd for storing the superblock
Description: 
         Creates the root directory using inode 1 and block 1.
------------------------------------------------------------------*/

int create_mroot(int mfd)
{

   /* lets create the inode for the directory */

   /* Store inode on disk  */

   /* Set bit in imap */
   
   /* lets fill directory table */

   /* store directory table on disk */

   /* Set bit in zmap */

   return(OK);
}
