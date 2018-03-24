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
   char root_dir[BLOCK_SIZE];  /* for creating the directory entry */
   char *rtpt = root_dir;
   struct minix_inode rtino;   /* root inode */
   char bit10 = 0x3;

   /* lets create the inode for the directory */
   memset(&rtino,0,INODESIZE);
   rtino.i_mode = S_IFDIR + 0755 ; /* setting the mode */
   rtino.i_uid = getuid();
   rtino.i_size = 2*DIRENTRYSIZE;
   rtino.i_time = time(NULL);
   rtino.i_gid = getgid();
   rtino.i_nlinks = 2;  
   rtino.i_zone[0] = FIRSTZONE;

   /* Store inode on disk  */
   if(lseek(mfd,(2+NUMIMAPBLOCKS+NUMZMAPBLOCKS)*BLOCK_SIZE,SEEK_SET) == -1) 
       { perror("mkfs.minix (lseek itable)"); return(ERR1); } /* to start of inode table */
   if(write(mfd,&rtino,INODESIZE) != INODESIZE)
       { perror("mkfs.minix(write itable)"); return(ERR1); } 

   /* Set bit in imap */
   if(lseek(mfd,2*BLOCK_SIZE,SEEK_SET)==-1)  /* to start of the inode map */
       { perror("mkfs.minix (lseek imap)"); return(ERR1); }
   if(write(mfd,&bit10,1) != 1)
       { perror("mkfs.minix(write imap)"); return(ERR1); }
   
   /* lets fill directory table */
   memset(root_dir,0,BLOCK_SIZE);  // set all bytes to zero
   *(__u16 *)root_dir = 1;   /* set ino in first record, 
                                casting required to point to 16 bit value */
   strcpy(&root_dir[2],".");   /* add the directory name */
   *(__u16 *) (&root_dir[DIRENTRYSIZE]) = 1;  /* Set ino in second record,
                                                 casting required to point to 16 bit value */
   strcpy(root_dir+DIRENTRYSIZE+2,".."); /* add the directory name */

   /* Store directory table on the disk  */
   if(lseek(mfd,FIRSTZONE*BLOCK_SIZE,SEEK_SET) == -1) /* to start of zones */
       { perror("mkfs.minix (lseek zone)"); return(ERR1); }
   if(write(mfd,root_dir,BLOCK_SIZE) != BLOCK_SIZE)
       { perror("mkfs.minix(write zone)"); return(ERR1); } 

   /* Set bit in zmap */
   if(lseek(mfd,(2+NUMIMAPBLOCKS)*BLOCK_SIZE,SEEK_SET) == -1) 
       { perror("mkfs.minix(lseek zmap)"); return(ERR1); }
   if(write(mfd,&bit10,1) != 1)
       { perror("mkfs.minix(write zmap)"); return(ERR1); }

   return(OK);
}
