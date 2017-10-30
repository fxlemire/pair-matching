Pair-Matching
=============
This is a simple and easy-to-customize program which allows to match people in pairs.

A good use would be for Secret Santa.

Enter the people you need to match together, and this program will send an e-mail to all participants telling them who is their pick.

# How To Use

Create a file `config.json` in your root directory with the following keys and without comments:

```
"SEND": Boolean // true will send the emails, false will print results to the console
"email": {
  // html string starting with the <body> tag. Use the variable giftReceiver to display the picked person
  "content": String
  "override": { // optional object that allows to override some email configurations
    [email: String]: { // email of the recipient taken from members array
      "subject": String // subject of the email to override
      "to": String // overrides the name that will be used, e.g. "name" <member@email.com>
    }
  },
  "subject": String // use the variable emailRecipient to display the name of the recipient
},
"members": [
  {
    "email": String,
    "name": String
  }
],
"rules": { // optional rules that will reject pairings
  "couples": [ // forbid member1 to pick member2 and vice-versa
    [member1.email, member2.email] // use the member emails
  ],
  "pastyears": [ // forbid member1 to pick member2
    [member1.email, member2.email] // use the member emails
  ]
},
"sender": {
  "from": String // as you want it to appear for the receivers,
  "host": String // smtp server,
  "port": String // smtp port,
  "username": String // sender email,
  "password": String // sender password
}
```

See [config.example.json](config.example.json);

Sender config example for Gmail:
```
{
  "from": "Santa Claus <santa@gmail.com>",
  "host": "smtp.gmail.com",
  "port": "587",
  "username": "santa@gmail.com",
  "password": "SantasMagicPassword"
}
```

Sender config example for Outlook:
```
{
  "from": "Santa Claus <santa@outlook.com>",
  "host": "smtp-mail.outlook.com",
  "port": "587",
  "username": "santa@outlook.com",
  "password": "SantasMagicPassword"
}
```

# Compile and run

```
javac -cp ".;libs/*" -d target src/*
java -cp "target;libs/*" Draw
```
