// Librarys importieren (Filesystem,Serversystem) etc

const express = require('express');
const bodyParser = require('body-parser');
const fs = require('fs');
const app = express();
const port = 3001;

// Javascript Hilfsfunktion die guckt ob die Struktur 2er Objekte gleich ist
// benutzt um die Struktur der Requests an den Server mit passenden Responeses anzugleichen 
function hasAllKeys(objToCheck, keysObj) {
  if(Object.keys(objToCheck).length != (Object.keys(keysObj).length)) return false
  return Object.keys(keysObj).every(key => objToCheck.hasOwnProperty(key));
}

// Ausgehend von einer dynamischen SessionID (ids.json) den passenden Account finden
// Primarykey: Email
function getEmailById(id){
  let sessionData = JSON.parse(fs.readFileSync('ids.json', 'utf-8'));
  console.log(sessionData)
  for(let data of sessionData){
    if(data.sessionID == id) return data.email
  }
  return null
}

// Ausgehend von der Hardware ID der Drone(drones.json) den passenden Account finden
function getUserByHardwareId(id){
  let dronesTable = JSON.parse(fs.readFileSync('drones.json', 'utf-8'));
  for(let entry of dronesTable){
    if(entry.hardwareID == id) return entry.user
  } 
}

app.use(bodyParser.json());

// Definitionen verschiedener möglicher Strukturen der Request des Clients
// Ausgehend von einer passenden Struktur werden verschieden Serveraktionen getätigt
const acc = {
  name:"Max",
  email:"maxmustermann@gmail.com",
  passwort:"passwort",
} 
const entry = {
  email:"email.com",
  passwort:"passwort",
}

const session = {
  sessionID: 100,
}

const accept = {
  acceptorSession: 100,
}

const ownersession = {
  ownerSession: 100,
}

const hardwareID = {
  hardwareID:100,
}

const drone = {
  hardwareID:100,
  sessionID:100,
}
const requestA = {
  sessionID: 100,
  receiver: "max@gmail.com",
}
const requestB = {
  sessionID: 100,
  receiver: "max@gmail.com",
  //hardwareID:100,
  geoString:"23N123O",
}

// Endpunkt für die Verwaltung von Dronen
app.post('/api/drones',(req,res) => {
  const requestData = req.body;
  if(hasAllKeys(requestData,drone)){ // Der Client möchte eine Drohne posten
    const userEmail = getEmailById(requestData.sessionID) // Die vom Client gegebene Session ID wird zum Finden des Users benutzt
    
    // Die gespeicherten Drohnen werden als Array geladen und durch die Drohne des Users ergänzt
    let dronesTable = JSON.parse(fs.readFileSync('drones.json', 'utf-8'))
    dronesTable.push({user:userEmail,hardwareID:requestData.hardwareID})
    fs.writeFileSync('drones.json', JSON.stringify(dronesTable, null, 2), 'utf-8');
    // Feedback zum Client
    res.status(200).json({message:"Succesfully created the drone"})
    return
  }
  else if(hasAllKeys(requestData,ownersession)){ // Der Client möchte seine Drohne getten
    const userEmail = getEmailById(requestData.ownerSession) // User ausgehend von SessionID ( erklärt oben)

		// Die gespeicherten Dronen werden geladen und durchsucht
    let dronesTable = JSON.parse(fs.readFileSync('drones.json', 'utf-8'));
    let drone;
    for(let entry_ of dronesTable){
      if(entry_.user == userEmail) drone = entry_.hardwareID
    }   
    // Gefundene Drohne wird zurück gegeben zum Client 
    res.status(200).json({drones:[{hardwareID:drone}]})
    return
  }
  // Falls die Request vom User ein unbekanntes Format hat wird dies züruckgegeben mit Statuscode 400
  res.status(400).json({message:"Wrong format"})
})

// Endpunkt für die Verwaltung von Requests/Deliveries
app.post('/api/requests',(req,res) => {
  const requestData = req.body;
  if(hasAllKeys(requestData,requestA)){ // Der Client möchte eine Request an einen anderen User stellen (hier noch ohne Koordinaten)
    const senderEmail = getEmailById(requestData.sessionID)
    // Gespeicherte Requests werden geladen, ergänzt und gespeichert
    let requestTable = JSON.parse(fs.readFileSync('requests.json', 'utf-8'));
    requestTable.push({sender:senderEmail,receiver:requestData.receiver,accepted:false})
    fs.writeFileSync('requests.json', JSON.stringify(requestTable, null, 2), 'utf-8');
    // Feedback zum User
    res.status(200).json({message:"Succesfully created the request A"})
    return
  }
  else if(hasAllKeys(requestData,requestB)){ // Der Client möchte eine Request stellen ( mit Koordinaten)
    const senderEmail = getEmailById(requestData.sessionID)
    let requestTable = JSON.parse(fs.readFileSync('requests.json', 'utf-8'));
    // vorherige Request (ohne Koordinaten) wird gelöscht
    requestTable = requestTable.filter((el) => !(el.sender == senderEmail && el.receiver == requestData.receiver))
    // Ergänzung nach bekanntem Schema,Feedback an User
    requestTable.push({sender:senderEmail,receiver:requestData.receiver,geoString:requestData.geoString})
    fs.writeFileSync('requests.json', JSON.stringify(requestTable, null, 2), 'utf-8');
    res.status(200).json({message:"Succesfully created the request B"})
    return
  }
  else if(hasAllKeys(requestData,accept)){ // Der Client möchte eine Request akzeptieren
    const receiverEmail = getEmailById(requestData.acceptorSession)
    let requestTable = JSON.parse(fs.readFileSync('requests.json', 'utf-8'));
    // Requests werden durchsucht und passende Request wird akzeptiert
    for(let request of requestTable){
      if(request.receiver == receiverEmail && request.accepted != undefined) request.accepted = true
    }
    // wurde jz genug erklärt hoffentlich klar
    fs.writeFileSync('requests.json', JSON.stringify(requestTable, null, 2), 'utf-8');
    res.status(200).json({message:"Succesfully accepted the request"})
    return
  }
  else if(hasAllKeys(requestData,hardwareID)){ // Die Drohne fordert ihre Ziele an
    let requestTable = JSON.parse(fs.readFileSync('requests.json', 'utf-8'));
    let coords = [] // Array aus Koordinaten ( Zielen der Drohne )
    let user = getUserByHardwareId(requestData.hardwareID) // User dem die Drohne zugewiesen ist
    for(let req of requestTable){
      if(req.sender == user) coords.push(req.geoString)
    }
    requestTable = requestTable.filter((req) => req.sender != user) // Request werden gelöscht (Drohne führt sie aus)
    fs.writeFileSync('requests.json', JSON.stringify(requestTable, null, 2), 'utf-8')
    res.status(200).json({coords:coords}) // Die Koordinaten werden zurück an die Drohne geschickt
    return
  }
  else if(hasAllKeys(requestData,session)){ // Der Client möchte seine Requests abrufen 
    const clientEmail = getEmailById(requestData.sessionID) // Der Client übergibt seine SessionID 
    let requestTable = JSON.parse(fs.readFileSync('requests.json', 'utf-8'));
    
    // Die Requests werden durchsucht, 
    // der Client bekommt eine Liste aus gestellten , und eine Liste aus erhaltenen Requests
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

// Endpunkt der Accounts
app.post('/api/users', (req, res) => {
  const requestData = req.body;
  console.log(requestData);
  if(hasAllKeys(requestData,acc) ){ // Der User möchte einen Account erstellen
    try {
      const existingData = fs.readFileSync('users.json', 'utf-8');
      const parsedData = JSON.parse(existingData);
      for(let user of parsedData){ // Es wird geguckt ob ein Account mit der Email bereits existiert
        console.log(user)
        if(user.email == requestData.email){
          console.log(`Found duplicate: ${requestData}`)
          // Der User wird darüber informiert, falls seine Email schon benutzt ist
          res.status(400).json({message:"No duplicates allowed, one user per email"}) 
          return
        }
      }
      // Ist die Email nicht in Benutzung wird der Account erstellt
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
  else if(hasAllKeys(requestData,entry)){ // Der User möchte sich einloggen
    let existingData = fs.readFileSync('users.json', 'utf-8');
    existingData = JSON.parse(existingData)
    // Es wird geguckt ob der User existiert
    if(existingData.map(data => JSON.stringify([data.email, data.passwort])).includes(JSON.stringify(Object.values(requestData)))){
      const sessionID = Math.floor(Math.random() * Date.now()); // Es wird eine SessionID erstellt
      let sessionData = JSON.parse(fs.readFileSync('ids.json', 'utf-8'));
      // Besitzt er schon eine Wird ihm das mitgeteilt
      for(let entry of sessionData){
        if(entry.email == requestData.email) {
          res.status(400).json({error:"You already have a session id"})
          return
        }
      }
      // Der User existiert und ist nicht eingeloggt , also wird ihm eine SessionID zugewiesen
      sessionData.push({
        email:requestData.email,
        sessionID:sessionID
      })
      // Diese wird zurück zum Client gegeben und in der Datenbank gesichert
      fs.writeFileSync('ids.json', JSON.stringify(sessionData, null, 2), 'utf-8');
      res.status(200).json({message:"Exists",sessionID:sessionID})
    }
    else{
      // Der User existiert nicht also kann er sich nicht einloggen
      res.status(200).json({message:"Doesnt exist"})
    }
    return;
  }
  
  else if(hasAllKeys(requestData,session)){ // Der User loggt sich aus
		// Er übergibt seine SessionID welche aus dem System gelöscht wird
    let sessionData = JSON.parse(fs.readFileSync('ids.json', 'utf-8'));
    console.log(sessionData)
    sessionData = sessionData.filter((obj) => obj.sessionID != requestData.sessionID)
    fs.writeFileSync('ids.json', JSON.stringify(sessionData, null, 2), 'utf-8');
    res.status(200).json({message:"Logged out"})
    return;
    
  }
  res.status(400).json({ error: 'Wrong data' });
});

// Server deployen
app.listen(port, () => {
  console.log(`Server is listening at http://localhost:${port}`);
});


app.get('/api/users', (req, res) => { // User abrufen
  try {
    const data = fs.readFileSync('users.json', 'utf-8');
    // Der Client bekommt ein Array aus allen Usern geschickt, hierbei wird natürlich nicht das Passwort übergeben
    const jsonData = JSON.parse(data).map(data => ({name: data.name, email: data.email}));
    res.status(200).json(jsonData);
    console.log("Succesfully received get request")
  } catch (error) {
    console.error('Error reading users.json:', error);
    res.status(500).json({ error: 'Internal Server Error' });
  }
});