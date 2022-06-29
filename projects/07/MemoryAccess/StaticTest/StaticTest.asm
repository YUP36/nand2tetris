// push constant 111
@111
D=A
@SP
A=M
M=D
@SP
M=M+1
// push constant 333
@333
D=A
@SP
A=M
M=D
@SP
M=M+1
// push constant 888
@888
D=A
@SP
A=M
M=D
@SP
M=M+1
// pop static 8
@SP
M=M-1
A=M
D=M
@Foo.8
M=D
// pop static 3
@SP
M=M-1
A=M
D=M
@Foo.3
M=D
// pop static 1
@SP
M=M-1
A=M
D=M
@Foo.1
M=D
// push static 3
@Foo.3
D=M
@SP
A=M
M=D
@SP
M=M+1
// push static 1
@Foo.1
D=M
@SP
A=M
M=D
@SP
M=M+1
// sub
@SP
AM=M-1
D=M
@SP
AM=M-1
M=M-D
@SP
M=M+1
// push static 8
@Foo.8
D=M
@SP
A=M
M=D
@SP
M=M+1
// add
@SP
AM=M-1
D=M
@SP
AM=M-1
M=M+D
@SP
M=M+1
(END)
@END
0;JMP