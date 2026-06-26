
@echo off
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo Java is NOT installed or not in PATH please install Java 21!
    pause
    exit /b
)
echo Java is successfully detected!
echo Getting Wget
winget install -e --id GNU.Wget2
echo Setting up server
wget2 -O paper.jar https://fill-data.papermc.io/v1/objects/5ffef465eeeb5f2a3c23a24419d97c51afd7dbb4923ff42df9a3f58bba1ccfba/paper-1.21.11-132.jar
wget2 -O eula.txt https://www.dropbox.com/scl/fi/vu7a1hdyvylfs3z525rnm/eula.txt?rlkey=t9d8cvzk7wodsalodqdpsru6o&st=1569lw6j&dl=1
echo java -Xmx2G -Xms2G -jar paper.jar > start.bat
mkdir plugins
wget2 -O plugins/packetEvents.jar https://cdn.modrinth.com/data/HYKaKraK/versions/h0ncTpUP/packetevents-spigot-2.13.0.jar?mr_download_reason=standalone&mr_game_version=1.21.11&mr_loader=paper
wget2 -O plugins/worldEdit.jar https://cdn.modrinth.com/data/1u6JkXh5/versions/yDUBafTJ/worldedit-bukkit-7.4.3.jar?mr_download_reason=standalone&mr_game_version=1.21.11&mr_loader=paper
wget2 -O plugins/betterModel.jar https://cdn.modrinth.com/data/4h8rX3rt/versions/8xoSUfzr/bettermodel-3.2.0-paper.jar?mr_download_reason=standalone&mr_game_version=1.21.11&mr_loader=paper
wget2 -O plugins/LibsDisguises.jar https://github.com/libraryaddict/LibsDisguises/releases/download/v11.0.18/LibsDisguises-11.0.18-Github.jar
wget2 -O plugins/ProtocolLib.jar https://github.com/dmulloy2/ProtocolLib/releases/download/5.4.0/ProtocolLib.jar
wget2 -O plugins/SkinsRestorer.jar https://cdn.modrinth.com/data/TsLS8Py5/versions/itZJnFwV/SkinsRestorer.jar?mr_download_reason=standalone&mr_game_version=1.21.11&mr_loader=paper
echo Setting up plugin!
wget2 -O plugins/Freedom.jar https://www.dropbox.com/scl/fi/440x6vbelxmoosjrzu3xl/Freedom-1.0-SNAPSHOT.jar?rlkey=c94tljy74i4oez3up3gdzuotj&st=9y2df3ga&dl=1

java -Xmx2G -Xms2G -jar paper.jar
echo Please Agree to Minecrafts EULA within this Folder then run start.bat
pause