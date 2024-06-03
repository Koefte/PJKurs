const express = require('express');
const bodyParser = require('body-parser');
const fs = require('fs');
const { request } = require('http');

const app = express();
const port = 3001;

function hasAllKeys(objToCheck, keysObj) {
  if(Object.keys(objToCheck).length != (Object.keys(keysObj).length)) return false
  return Object.keys(keysObj).every(key => objToCheck.hasOwnProperty(key));
}

function getEmailById(id){
  let sessionData = JSON.parse(fs.readFileSync('ids.json', 'utf-8'));
  console.log(sessionData)
  for(let data of sessionData){
    if(data.sessionID == id) return data.email
  }
  return null
}

function getUserByHardwareId(id){
  let stationsTable = JSON.parse(fs.readFileSync('stations.json', 'utf-8'));
  for(let entry of stationsTable){
    if(entry.hardwareID == id) return entry.user
  } 
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
  sessionID: 100
}

const accept = {
  acceptorSession: 100
}

const hardwareID = {
  hardwareID:100
}

const station = {
  hardwareID:100,
  sessionID:100
}

const requestA = {
  sessionID: 100,
  receiver: "max@gmail.com"
}


const requestB = {
  sessionID: 100,
  receiver: "max@gmail.com",
  hardwareID:100,
  geoString:"23N123O",
}

let requestTable = JSON.parse(fs.readFileSync('./requests.json', 'utf-8'));
let coords = []
let user = getUserByHardwareId(100)
for(let req of requestTable){
  if(req.sender == user) coords.push(req.geoString)
}


app.post('/api/stations',(req,res) => {
  const requestData = req.body;
  if(hasAllKeys(requestData,station)){
    const userEmail = getEmailById(requestData.sessionID)
    let stationsTable = JSON.parse(fs.readFileSync('stations.json', 'utf-8'));
    stationsTable.push({user:userEmail,station:hardwareID})
    fs.writeFileSync('stations.json', JSON.stringify(stationsTable, null, 2), 'utf-8');
    res.status(200).json({message:"Succesfully created the station"})
    return
  }
  res.status(400).json({message:"Wrong format"})
})

app.get('/api/stations',(req,res) => {
  const requestData = req.body;
  if(hasAllKeys(requestData,session)){
    const userEmail = getEmailById(requestData.sessionID)
    let stationsTable = JSON.parse(fs.readFileSync('stations.json', 'utf-8'));
    let station;
    for(let entry in stationsTable){
      if(entry.user == userEmail) station = entry.hardwareID
    }   
    res.status(200).json({station:station})
    return
  }
  res.status(400).json({message:"Wrong format"})
})

app.post('/api/requests',(req,res) => {
  const requestData = req.body;
  if(hasAllKeys(requestData,requestA)){
    const senderEmail = getEmailById(requestData.sessionID)
    let requestTable = JSON.parse(fs.readFileSync('requests.json', 'utf-8'));
    requestTable.push({sender:senderEmail,receiver:requestData.receiver,accepted:false})
    fs.writeFileSync('requests.json', JSON.stringify(requestTable, null, 2), 'utf-8');
    res.status(200).json({message:"Succesfully created the request A"})
    return
  }
  else if(hasAllKeys(requestData,requestB)){
    const senderEmail = getEmailById(requestData.sessionID)
    let requestTable = JSON.parse(fs.readFileSync('requests.json', 'utf-8'));
    requestTable.push({sender:senderEmail,receiver:requestData.receiver,geoString:requestData.geoString})
    fs.writeFileSync('requests.json', JSON.stringify(requestTable, null, 2), 'utf-8');
    res.status(200).json({message:"Succesfully created the request B"})
    return
  }
  else if(hasAllKeys(requestData,accept)){
    const receiverEmail = getEmailById(requestData.acceptorSession)
    let requestTable = JSON.parse(fs.readFileSync('requests.json', 'utf-8'));
    for(let request of requestTable){
      if(request.receiver == receiverEmail && request.accepted != undefined) request.accepted = true
    }
    fs.writeFileSync('requests.json', JSON.stringify(requestTable, null, 2), 'utf-8');
    res.status(200).json({message:"Succesfully accepted the request"})
    return
  }
  else if(hasAllKeys(requestData,hardwareID)){
    let requestTable = JSON.parse(fs.readFileSync('requests.json', 'utf-8'));
    let coords = []
    let user = getUserByHardwareId(requestData.hardwareID)
    for(let req of requestTable){
      if(req.sender == user) coords.push(req.geoString)
    }
    res.status(200).json({coords:coords})
    return
  }
  else if(hasAllKeys(requestData,session)){
    const clientEmail = getEmailById(requestData.sessionID)
    let requestTable = JSON.parse(fs.readFileSync('requests.json', 'utf-8'));
    let incomingRequests = []
    let outgoingRequests = []
    console.log(clientEmail)
    for(let req of requestTable){
      if(req.sender == clientEmail) outgoingRequests.push(req)
      if(req.receiver == clientEmail) incomingRequests.push(req)
    }
    res.status(200).json({out:outgoingRequests,in:incomingRequests})
    return
  }
  res.status(400).json({message:"Wrong format"})
})

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
      res.status(200).json({message:"Succesfully created the account"})
      return
    } catch (error) {
      console.error('Error appending data to users.json:', error);
      res.status(500).json({ error: 'Internal Server Error' });
      return;
    }
  
  }
  else if(hasAllKeys(requestData,entry)){

    let existingData = fs.readFileSync('users.json', 'utf-8');
    existingData = JSON.parse(existingData)

    if(existingData.map(data => JSON.stringify([data.email, data.passwort])).includes(JSON.stringify(Object.values(requestData)))){
      const sessionID = Math.floor(Math.random() * Date.now());
      let sessionData = JSON.parse(fs.readFileSync('ids.json', 'utf-8'));
      for(let entry of sessionData){
        if(entry.email == requestData.email) {
          res.status(400).json({error:"You already have a session id"})
          return
        }
      }
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