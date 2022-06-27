// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)
//
// This program only needs to handle arguments that satisfy
// R0 >= 0, R1 >= 0, and R0*R1 < 32768.

// Put your code here.
@0
D=M
@i
M=D	// i=R0
@sum	
M=0	// sum=0

(LOOP)
@END
D;JEQ	// if D==0, then go to end
@i
M=M-1	// i--
@1
D=M	// D=R1
@sum
M=M+D	// sum+=R1
@i
D=M	// D=i
@LOOP
0;JMP

(END)
@sum
D=M
@2
M=D	// R2=sum

@END
0;JMP