# GeneralLedger

## Cloud9 SetUp

### .bashrc
```
export GRADLE_HOME=/opt/gradle/gradle-9.5.1
export PATH=$PATH:$GRADLE_HOME/bin
export PATH=$PATH:$HOME/.npm-global/bin:$PATH
export NVM_DIR="$HOME/.nvm"
if [ -s "$NVM_DIR/nvm.sh" ]; then
        . "$NVM_DIR/nvm.sh"
fi

export PATH="$HOME/.jenv/bin:$PATH"
eval "$(jenv init -)"
```

### Docker
1. ```sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose```
2. ```sudo chmod +x /usr/local/bin/docker-compose```
3. ```docker login```

### Gradle
1. ```wget https://services.gradle.org/distributions/gradle-9.5.1-bin.zip```
2. ```sudo unzip gradle-9.5.1-bin.zip -d /opt/gradle```
3. ```vi ~/.bashrc```
4. ```export GRADLE_HOME=/opt/gradle/gradle-9.5.1```
5. ```export PATH=$PATH:$GRADLE_HOME/bin```
6. ```save .bashrc```
7. ```source ~/.bashrc```

### NVM
1. ```vi ~/bashrc```
2. 
```
if [ -s "$NVM_DIR/nvm.sh" ]; then
        . "$NVM_DIR/nvm.sh"
fi
```
3. ```save .bashrc```
4. ```source ~/.bashrc```
5. ```curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.5/install.sh | bash```
6. ```nvm install 24```
7. ```nvm use 24```
8. ```nvm alias default 24```

### Angular
1. ```mkdir -p ~/.npm-global```
2. ```npm config set prefix "$HOME/.npm-global"```
3. ```vi ~/bashrc```
4. ```export PATH=$PATH:$HOME/.npm-global/bin:$PATH```
5. ```save .bashrc```
6. ```source ~/.bashrc```
7. ```npm install -g @angular/cli```

### Git
1. ```Git clone <https>```
2. ```git remote set-url origin https://<username>:<your-token>@github.com/username/financial.git```

### JAVA
1. ```sudo dnf install -y java-21-amazon-corretto-devel```
2. ```git clone https://github.com/jenv/jenv.git ~/.jenv```
3. ```vi ~/.bashrc```
4. ```export PATH="$HOME/.jenv/bin:$PATH"```
5. ```eval "$(jenv init -)"```
6. ```save .bashrc```
7. ```source ~/.bashrc```
8. ```ls -d /usr/lib/jvm/*```
9. ```jenv add <other version(s) of java>>```
10. ```jenv add /usr/lib/jvm/java-21-amazon-corretto.x86_64```
11. ```jenv versions```
12. ```jenv global 21```
13. ```jenv enable-plugin export```
14. ```Source ~/.bashrc```