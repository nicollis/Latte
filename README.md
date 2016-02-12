# README #

Latte was a basic-like scripting language I made while learning Java 

It recognizes classes, functions, and has both static and dynamic variables. This project is the interpreter I wrote in Java that would read .latte files


An example of Latte is below


###Latte Example



```
#!java

PRINT "Hello from latte!"

Booda = 0
INT XYZ #variables can be defined with no value
Boat = 0.85
foo = 40

FOR XYZ = 1 TILL 10
#this is a comment
	RUN first1 #this is another comment
NEXT

[STRING] XYZ = "Honalulu"

RUN Second UNTIL Booda = 7

RUN thIrd

RUN printall

#DESTROY XYZ

RUN printall

EXIT

define Second 
	PRINT Boat; Booda
	Booda = Booda + Boat
RETURN

DEFINE first1
		PRINT XYZ
	IF (XYZ = 5 && Booda = 0) OR foo = 41 THEN 
		PRINT "IF STATEMENT" 
	ELSE IF XYZ = 6 && (Booda = 1 OR foo = 40) THEN
		PRINT "ELSE STATEMENT" 
	ELSE IF XYZ OR Booda = 1 THEN
		PRINT "Last ELSE"
	ENDIF
									 
RETURN



Define thIrd 
	PRINT foo
	foo = XYZ
	PRINT foo
	foo = Boat
	PRINT foo
RETURN

define printall
	print
	print "PRINT ALL"
	print "Booda"; Booda
	print "XYZ"; XYZ
	print "Boat"; Boat
	print "foo"; foo
return

PRINT "Class Test"

	c = new car
	
	PRINT c.milage
	PRINT c.color
	c.start
	PRINT c.i.color
	
	
EXIT

CLASS car

	STRING color = "red"
	INT milage = 150000
	STRING make
	STRING model
	i = new inside
	
	
	DEFINE start
		PRINT "Car starts"
	RETURN
	
	OBJECT inside
		STRING color = "Black"
		INT numOfCupHolders = 4
	ENDOBJ

ENDCLASS

PRINT "Object Test"

	c = new truck
	c.color = "green"
	c.make = "ford"
	
	b = new car
	b.make = "Hyndia"
	b.milage = c.milage
	
	
PRINT c.milage
PRINT c.color
PRINT c.chasseStyle
PRINT c.bedStyle
	c.towingClass = 2
PRINT "Towing class", c.towingClass
PRINT "NEW OBJECT"
PRINT b.make
PRINT b.milage
PRINT b.model

EXIT


OBJECT car

	STRING color = "red"
	INT milage = 150000
	STRING make
	STRING model

ENDOBJ

OBJECT truck EXTENDS car
	
	chasseStyle = "CrewCab"
	bedStyle = "Sidesteps"
	towingClass = 1

ENDOBJ
```
