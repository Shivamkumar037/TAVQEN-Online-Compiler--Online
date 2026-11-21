
package com.compiler.backend.service;

import com.compiler.backend.dto.LanguageDTO;
import com.compiler.backend.utility.Languages;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LanguageService {

    public LanguageDTO getLanguages() {

        List<Languages> languages = new ArrayList<>();

        languages.add(Languages.builder()
                .id(1)
                .name("JAVA")
                .extension("java")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/openjdk")
                .defaultCode("public class Main {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        System.out.println(\"Hello TAVQEN Coder\");\n" +
                        "    }\n" +
                        "}")
                .build());

        languages.add(Languages.builder()
                .id(2)
                .name("PYTHON")
                .extension("py")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/python/3776AB")
                .defaultCode("print(\"Hello TAVQEN Coder\")")
                .build());

        languages.add(Languages.builder()
                .id(3)
                .name("JAVASCRIPT")
                .extension("js")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/javascript/F7DF1E")
                .defaultCode("console.log(\"Hello TAVQEN Coder\");")
                .build());

        languages.add(Languages.builder()
                .id(4)
                .name("C")
                .extension("c")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/c/A8B9CC")
                .defaultCode("#include <stdio.h>\n\n" +
                        "int main() {\n" +
                        "    printf(\"Hello TAVQEN Coder\\n\");\n" +
                        "    return 0;\n" +
                        "}")
                .build());

        languages.add(Languages.builder()
                .id(5)
                .name("C++")
                .extension("cpp")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/cplusplus/00599C")
                .defaultCode("#include <iostream>\n\n" +
                        "int main() {\n" +
                        "    std::cout << \"Hello TAVQEN Coder\" << std::endl;\n" +
                        "    return 0;\n" +
                        "}")
                .build());

        languages.add(Languages.builder()
                .id(6)
                .name("C#")
                .extension("cs")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/csharp/512BD4")
                .defaultCode("using System;\n\n" +
                        "class Program {\n" +
                        "    static void Main() {\n" +
                        "        Console.WriteLine(\"Hello TAVQEN Coder\");\n" +
                        "    }\n" +
                        "}")
                .build());

        languages.add(Languages.builder()
                .id(7)
                .name("PHP")
                .extension("php")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/php/777BB4")
                .defaultCode("<?php\n" +
                        "echo \"Hello TAVQEN Coder\";\n" +
                        "?>")
                .build());

        languages.add(Languages.builder()
                .id(8)
                .name("TYPESCRIPT")
                .extension("ts")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/typescript/3178C6")
                .defaultCode("const message: string = \"Hello TAVQEN Coder\";\n" +
                        "console.log(message);")
                .build());

        languages.add(Languages.builder()
                .id(9)
                .name("GO")
                .extension("go")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/go/00ADD8")
                .defaultCode("package main\n\n" +
                        "import \"fmt\"\n\n" +
                        "func main() {\n" +
                        "    fmt.Println(\"Hello TAVQEN Coder\")\n" +
                        "}")
                .build());

        languages.add(Languages.builder()
                .id(10)
                .name("RUST")
                .extension("rs")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/rust/DEA584")
                .defaultCode("fn main() {\n" +
                        "    println!(\"Hello TAVQEN Coder\");\n" +
                        "}")
                .build());

        languages.add(Languages.builder()
                .id(11)
                .name("KOTLIN")
                .extension("kt")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/kotlin/7F52FF")
                .defaultCode("fun main() {\n" +
                        "    println(\"Hello TAVQEN Coder\")\n" +
                        "}")
                .build());

        languages.add(Languages.builder()
                .id(12)
                .name("SWIFT")
                .extension("swift")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/swift/F05138")
                .defaultCode("print(\"Hello TAVQEN Coder\")")
                .build());

        languages.add(Languages.builder()
                .id(13)
                .name("RUBY")
                .extension("rb")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/ruby/CC342D")
                .defaultCode("puts \"Hello TAVQEN Coder\"")
                .build());

        languages.add(Languages.builder()
                .id(14)
                .name("DART")
                .extension("dart")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/dart/0175C2")
                .defaultCode("void main() {\n" +
                        "    print('Hello TAVQEN Coder');\n" +
                        "}")
                .build());

        languages.add(Languages.builder()
                .id(15)
                .name("R")
                .extension("r")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/r/276DC3")
                .defaultCode("print(\"Hello TAVQEN Coder\")")
                .build());

        languages.add(Languages.builder()
                .id(16)
                .name("SQL")
                .extension("sql")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/mysql/4479A1")
                .defaultCode("SELECT 'Hello TAVQEN Coder' AS message;")
                .build());

        languages.add(Languages.builder()
                .id(17)
                .name("SCALA")
                .extension("scala")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/scala/DC322F")
                .defaultCode("object Main {\n" +
                        "    def main(args: Array[String]): Unit = {\n" +
                        "        println(\"Hello TAVQEN Coder\")\n" +
                        "    }\n" +
                        "}")
                .build());

        languages.add(Languages.builder()
                .id(18)
                .name("BASH")
                .extension("sh")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/gnubash/4EAA25")
                .defaultCode("#!/bin/bash\n" +
                        "echo \"Hello TAVQEN Coder\"")
                .build());

        languages.add(Languages.builder()
                .id(19)
                .name("PERL")
                .extension("pl")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/perl/39457E")
                .defaultCode("print \"Hello TAVQEN Coder\\n\";")
                .build());

        languages.add(Languages.builder()
                .id(20)
                .name("LUA")
                .extension("lua")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/lua/2C2D72")
                .defaultCode("print(\"Hello TAVQEN Coder\")")
                .build());

        languages.add(Languages.builder()
                .id(21)
                .name("HASKELL")
                .extension("hs")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/haskell/5D4F85")
                .defaultCode("main :: IO ()\n\n" +
                        "main = putStrLn \"Hello TAVQEN Coder\"")
                .build());

        languages.add(Languages.builder()
                .id(22)
                .name("ELIXIR")
                .extension("ex")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/elixir/4B275F")
                .defaultCode("IO.puts(\"Hello TAVQEN Coder\")")
                .build());

        languages.add(Languages.builder()
                .id(23)
                .name("ERLANG")
                .extension("erl")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/erlang/A90533")
                .defaultCode("-module(main).\n" +
                        "-export([start/0]).\n\n" +
                        "start() ->\n" +
                        "    io:format(\"Hello TAVQEN Coder~n\").")
                .build());

        languages.add(Languages.builder()
                .id(24)
                .name("JULIA")
                .extension("jl")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/julia/9558B2")
                .defaultCode("println(\"Hello TAVQEN Coder\")")
                .build());

        languages.add(Languages.builder()
                .id(25)
                .name("FORTRAN")
                .extension("f90")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/fortran/734F96")
                .defaultCode("program main\n" +
                        "    print *, \"Hello TAVQEN Coder\"\n" +
                        "end program main")
                .build());

        languages.add(Languages.builder()
                .id(26)
                .name("COBOL")
                .extension("cob")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/cobol/005CA5")
                .defaultCode("IDENTIFICATION DIVISION.\n" +
                        "PROGRAM-ID. MAIN.\n" +
                        "PROCEDURE DIVISION.\n" +
                        "    DISPLAY 'Hello TAVQEN Coder'.\n" +
                        "    STOP RUN.")
                .build());

        languages.add(Languages.builder()
                .id(27)
                .name("PASCAL")
                .extension("pas")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/freepascal/FFFFFF")
                .defaultCode("program Main;\n" +
                        "begin\n" +
                        "    writeln('Hello TAVQEN Coder');\n" +
                        "end.")
                .build());

        languages.add(Languages.builder()
                .id(28)
                .name("OBJECTIVE-C")
                .extension("m")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/apple/000000")
                .defaultCode("#import <Foundation/Foundation.h>\n\n" +
                        "int main() {\n" +
                        "    @autoreleasepool {\n" +
                        "        NSLog(@\"Hello TAVQEN Coder\");\n" +
                        "    }\n" +
                        "    return 0;\n" +
                        "}")
                .build());

        languages.add(Languages.builder()
                .id(29)
                .name("ASSEMBLY")
                .extension("asm")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/assemblyscript/007AAC")
                .defaultCode("section .data\n" +
                        "    message db 'Hello TAVQEN Coder', 10\n" +
                        "    length equ $ - message\n\n" +
                        "section .text\n" +
                        "    global _start\n\n" +
                        "_start:\n" +
                        "    mov rax, 1\n" +
                        "    mov rdi, 1\n" +
                        "    mov rsi, message\n" +
                        "    mov rdx, length\n" +
                        "    syscall\n\n" +
                        "    mov rax, 60\n" +
                        "    xor rdi, rdi\n" +
                        "    syscall")
                .build());

        languages.add(Languages.builder()
                .id(30)
                .name("SOLIDITY")
                .extension("sol")
                .version("Latest")
                .icon("https://cdn.simpleicons.org/solidity/363636")
                .defaultCode("// SPDX-License-Identifier: MIT\n" +
                        "pragma solidity ^0.8.20;\n\n" +
                        "contract Main {\n" +
                        "    function message() public pure returns (string memory) {\n" +
                        "        return \"Hello TAVQEN Coder\";\n" +
                        "    }\n" +
                        "}")
                .build());

        LanguageDTO languageDTO = new LanguageDTO();
        languageDTO.setLanguages(languages);

        return languageDTO;
    }
}
