# Freedom Minecraft Plugin
## **Paper 1.21.11**
### This plugin is deployed at [play.yaszu.xyz](play.yaszu.xyz)
### THIS PLUGIN HAS DOCUMENTATION AT [docs.yaszu.xyz](https://docs.yaszu.xyz)
>Named After a Song I was listening to with the freedom motif in January.

[![CodeFactor](https://www.codefactor.io/repository/github/yaszu-mage/freedom/badge)](https://www.codefactor.io/repository/github/yaszu-mage/freedom)
![HackaTime](https://hackatime.hackclub.com/api/v1/badge/U0B1MV9NZFU/Yaszu-Mage/Freedom)

This project requres these dependencies;
- Packet Events
- World Edit
- Skins Restorer
- Lib's Disguises
- BetterModel
- ProtocolLib

If you just want to build the project from source, just head to [Actually building the project](#actually-building)

***YOU WILL BE STRIPPED FROM ADMIN EVERY TIME YOU JOIN THE SERVER, THIS IS THE SUDO SYSTEM IN PLAY, YOU WILL NEED TO RE OP YOURSELF, THIS IS MEANT FOR ACTUALY SERVER APPLICATIONS, sorry***
### Inspirations
When creating this project I was inspired by a miriad of media ranging from the hit game *Undertale*, all the way to video series such as the backrooms.
### Why Create this?
This project has been made for my summer friend based Minecraft server, I wanted to have a server with all the features created by me so I could debug them. I would often run into errors and problems I could not fix myself, because I did not create the program. I thought to myself...
> If I could create the plugin, maybe I could fix it significantly faster if everything went wrong? 
## What is this plugin?
This is a comprehensive plugin comprised of multiple subsystems to achieve a couple basic concepts. This plugin implements;
- Life System with things to do after death
- 10 movesets normally obtainable
- Expandable Item System
- Word Based Magic (Expandable)
- Every System being modulable, allowing me to expand on it later. 
- Sudo system seperating ADMIN profiles with their default Profiles so they can player survival with everyone else
### Life System
Within the life system there is a hardprogramed value for the default amount of lives (9). This system fully ensures that the player lives are only subtracted IF the player dies by another player. The reason I implemented it in this fashion instead of in a "hardcore" way is because neither myself, nor my friends would last a ***DAY*** if we had to play hardcore. It would not be fun. Though I still wanted *death* to mean something, especially that by a player. So I implemented the life system, with actions players can do, even if they are wiped off the face of the world. When players die, they become ghosts, they may not interact with the world in anyway, though they are still, in effect, inside of the world. They can techinically still roam, though I implemented a system so they could have more *influence*.
### Soul Imbuement (Not 100% done)
When someone becomes a ghost, they can imbue their *soul* within someone else's weapon. This provides buffs to the person weilding the weapon, when the abilities are used.
### Movesets 
Within this plugin, there are 20 ***OBTAINABLE*** movesets that you can get within a survival experience, but there are 22 using admin commands. There are 10 *natural* movesets available when joining the server, and they are all named after colors. There is:
- Blue
- Red
- Purple
- Mocha
- Cafe
- Green
- Black
- Orange
- Yellow
They each have unique movesets and abilities that can help set the player apart from others. Some are mainly meant for attack, while others are meant for support. I have not found a reliable way to implement defense. 
### Word based Magic
Within this plugin, there is a fully fleshed out magic system, with mobile casting aswell as ritual circles you can find or create. Within the subsystem, there is a set of words that are then parsed, then turned into real actions that can happen within the world. The current list of keywords include:
- Location
- Delay
- Destruction
- Teleportation
- Amplification
- Area
- Effect
- Range
- Shock
- moveset
- Province
- up
- down
- look
- rain
- sun
- thundering
- regeneration
- haste
- and much more
You can chain these keywords, for example here is a simple destruction spell:
`destruction location 420 70 420 range 1`
Then you can amplify it by using:
`destruction location 420 70 420 range 25 amplification`
Though you could make it stronger through more creative attempts and stacking to try and keep costs down
### Sudo System
Within the project, I wanted to be able to play the server while still having creative access and moderator access if something got out of hand, so I implemented the sudo system to *ACT* as a seperate account, without having to pay for one. All information is stored within the persistent data container, and all admins are ***HARDCODED*** to try and prevent tampering. ~~(also I am lazy...)~~
### [Actually Building the Project](#actually-building-the-project)
Create a clone of the project by using:
`git clone https://github.com/Yaszu-Mage/Freedom`
Then build the project by doing within the directory
Replace "path/to/output" to the directory you want to build it to
`./gradlew build -PshadowJarPath="path/to/output"`
It should build a shadow jar, please reach out to me on Discord if there are any issues. ```Yaszu``` on discord :3

### Quick Setup
I have created a quick setup script for Windows, if you do not want to try and attempt to setup the server yourself because it is boring as heck... make a new file and paste in the contents listed [here](https://raw.githubusercontent.com/Yaszu-Mage/Freedom/refs/heads/master/setup.bat) filename should be "setup.bat" (without the quotes)

If you want to run it again, the generated run start.bat :D

### BTW IF YOU ARE A REVIEWER, MY HACKATIME IS WEIRD BC I TRIED TO MERGE WITH MY WAKA TIME AND I HAD THE BROWSER EXTENSION, I ALREADY SUBMITTED MULTIPLE TICKETS TO GET THIS FIXED TO NO AVAIL PLEASE DONT SHOOT ME DEAD ###
