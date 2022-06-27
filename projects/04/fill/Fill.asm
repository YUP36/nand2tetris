// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.

(INF)
@8191
D=A	// D=32
@i
M=D	// i=32
@24576
D=M	// D=R[24576]

@ELSE
D;JNE	// if R[24576]!=0, go to ELSE

(LOOPA)
@i
D=M	// D=i
@ENDA
D;JLT	// if i<0, go to ENDA

@16384
D=A	// D=16384
@i
A=D+M	// A=16384+i
M=0	// R[A]=0

@i
M=M-1	// i-=1

@LOOPA
0;JMP	// go to LOOPA

(ELSE)

(LOOPB)
@i
D=M	// D=i
@ENDB
D;JLT	// if i<0, go to ENDB

@16384
D=A	// D=16384
@i
A=D+M	// A=16384+i
M=0	// R[A]=0
M=!M	// R[A]=!0

@i
M=M-1	// i-=1

@LOOPB
0;JMP	// go to LOOPB

(ENDA)
(ENDB)
@INF
0;JMP	// go to INF