
### Generative Movement Language
by Cristian Vogel

A token based score generator which uses context-free grammars
and (optional) word lists to generate scores. By working with tokens
as the mode of data input and output, a score can be assigned meaning
through interpration by human performers and/or software agents

GML system and codebase was conceived and maintained by Cristian Vogel
with essential contributions from Daniel Howe of RiTa lib and MDK of Korisna Media
<br>

##### Note: RiTa grammars must now be in proper JSON or YAML format

<br>

### In IntelliJ Idea
#### Entry point TestGrammerGML.java 

Generative Movement Language is a context-free grammar generator.
 
 It uses a lexicon of movement language, based loosely on 
 Ann Hutchinson Guest's descriptions in the book 'Labanotation'.
 
 The program loads in  wordLists and builds the definitions of its  grammar  
 dynamically, importing all .txt files it finds in the /data/wordLists directory 
 
 It can interchange JSON grammar files ( in /data/grammarFiles) randomly 
 and repopulate them with definitions before expanding
 

 
 Code maintained by Cristian Vogel 2010 - 2019
 with essential contributions from Daniel C.Howe and MDK (aka Normalised)
 
 RiTa natural language library by Daniel C. Howe
 http://www.rednoise.org/rita/


