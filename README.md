# Chatière
Chatière is an over-complicated and over-engineered p2p chat application. It is dirty and ugly application that I created to serve as POC for a few concepts/ideas. Initially I planned to write this using Java8 to play with lambdas and funny stuff like that, but due to some technological restrictions, I had to "downgrade" to Java7.


## Chaton
Chaton will be the application launcher that will support auto-updates and such. It's not implemented yet, so don't waste your time here.

## Chatte
Chatte - pun intended - is the main chat application. The key features are:

 * Decentralized architecture
 * Secured connection (SSL)
 * Image sharing between clients
 * Automatically connect to known friends
 * Panic button
 
Basically I wanted to experiment with SSLSockets, Swing and animated GIF, reinvent the wheel by mixing Observer and Messaging patterns, decentralized P2P network. The code itself is crappy but I don't care. This is one of those write once, run once (or twice) situations.

[By the way, I'm and engineer, so don't expect a fancy Interface.](http://dilbert.com/strip/2002-09-24)

## Build and run

You will need git, jdk7 and maven.

Clone the repository:
```sh
git clone https://github.com/olopes/chatiere.git
```

Build it:
```sh
cd chatiere
mvn package
```

When the build finishes, open the chatte folder:
```sh
cd chatte
```

Generate a self-signed RSA certificate if you need to. Store name and password must be ssl.key unless you change the code.
```sh
keytool -genkey -alias ssl.key -keyalg RSA -keypass ssl.key -storepass ssl.key -keystore ssl.key
```

Run it!
```sh
java -jar target/chatte-0.0.1-SNAPSHOT.jar
```
or
```sh
java -cp target/chatte-0.0.1-SNAPSHOT.jar chatte.Chatte
```

Manualy to a friend for the first time by clicking "settings" icon and then typing your friend's host name and port. Currently both friends must be sharing the same SSL key, ie, the same self-signed certificate generated before.

### Notes
None of the images used are mine, I picked them somewhere in the Internet. I will try to credit the authors as soon as possible.

Cat upside down - [iconka.com](http://iconka.com/)

Other icons from [icons8.com](https://icons8.com/icon/5415/paste)

[Smiley with sunglasses](https://www.zazzle.com/cool_smiley_face_with_sunglasses_classic_round_sticker-217384233511350916) 
