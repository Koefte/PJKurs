const express = require('express');
const bodyParser = require('body-parser');
const fs = require('fs');
const { request } = require('http');

const app = express();
const port = 3001;

function hasAllKeys(objToCheck, keysObj) {
  return Object.keys(keysObj).every(key => objToCheck.hasOwnProperty(key));
}



app.use(bodyParser.json());

const acc = {
  name:"Max",
  email:"maxmustermann@gmail.com",
  passwort:"passwort"
} 

const entry = {
  email:"email.com",
  passwort:"passwort"
}

const session = {
  sessionID: 100,
}

// POST request handler
app.post('/api/users', (req, res) => {
  const requestData = req.body;

  console.log(requestData);

  if(hasAllKeys(requestData,acc) ){
    try {
      const existingData = fs.readFileSync('users.json', 'utf-8');
      const parsedData = JSON.parse(existingData);
      for(let user of parsedData){
        console.log(user)
        if(user.email == requestData.email){
          console.log(`Found duplicate: ${requestData}`)
          res.status(400).json({message:"No duplicates allowed, one user per email"})
          return
        }
      }
      parsedData.push(requestData);
      fs.writeFileSync('users.json', JSON.stringify(parsedData, null, 2), 'utf-8');
      console.log('Data appended to users.json');
    } catch (error) {
      console.error('Error appending data to users.json:', error);
      res.status(200).json({ error: 'Internal Server Error' });
      return;
    }
  
  }
  else if(hasAllKeys(requestData,entry)){

    let existingData = fs.readFileSync('users.json', 'utf-8');
    existingData = JSON.parse(existingData)

    if(existingData.map(data => JSON.stringify([data.email, data.passwort])).includes(JSON.stringify(Object.values(requestData)))){
      const sessionID = Math.random() * Date.now();
      let sessionData = JSON.parse(fs.readFileSync('ids.json', 'utf-8'));
      sessionData.push({
        email:requestData.email,
        sessionID:sessionID
      })
      fs.writeFileSync('ids.json', JSON.stringify(sessionData, null, 2), 'utf-8');
      res.status(200).json({message:"Exists",sessionID:sessionID})
    }
    else{
      res.status(200).json({message:"Doesnt exist"})
    }
    return;
  }

  
  else if(hasAllKeys(requestData,session)){
    let sessionData = JSON.parse(fs.readFileSync('ids.json', 'utf-8'));
    console.log(sessionData)
    sessionData = sessionData.filter((obj) => obj.sessionID != requestData.sessionID)
    fs.writeFileSync('ids.json', JSON.stringify(sessionData, null, 2), 'utf-8');
    res.status(200).json({message:"Logged out"})
    return;
    
  }


  res.status(400).json({ error: 'Wrong data' });
});

app.listen(port, () => {
  console.log(`Server is listening at http://localhost:${port}`);
});

app.get('/api/users', (req, res) => {
  try {
    const data = fs.readFileSync('users.json', 'utf-8');
    const jsonData = JSON.parse(data).map(data => ({name: data.name, email: data.email}));

    res.status(200).json(jsonData);
    console.log("Succesfully received get request")
  } catch (error) {
    console.error('Error reading users.json:', error);
    res.status(500).json({ error: 'Internal Server Error' });
  }
});