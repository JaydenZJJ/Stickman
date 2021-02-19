CONFIG:

A field has been added to indicate total number of levels available

"game" : {
        "levelcount" : 3,
        "screensize" : [640, 400]
},

Each level is specified using a number, starting from 1 to level count specified above.

 "levels" : {
    "1" : {

For the hero, an addtional field is used to specify the number of health points hero have.

"hero" : {"x" :  250.0, "y" :  50.0, "small" : true, "hp" : 5},

In level, there is also a field to specify the expected finish time for that particular level.

"finishTime" : 60


HOW TO RUN:

gradle run


FEATURES IMPLEMENTED:

Level Transition
Score and Time
Save and Load

PATTERNS:

Singleton - Entities/Hero

Observer - (Observer: Level/LevelImp) - (Subject: Entities/Slime, mushroom)

Memento - （Originator: model/GameEngineImpl) - (Memento - mode/Memento) - (CareTaker - model/CareTaker)

KEYS FOR S/L:

Q for Quicksave
E for Quickload

Pressing E when there is no save will output the message "no save" in console and no effects in the game.


