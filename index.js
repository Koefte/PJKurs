const express = require('express');
const bodyParser = require('body-parser');
const fs = require('fs');

const app = express();
const port = 3000;

function hasAllKeys(objToCheck, keysObj) {
  return Object.keys(keysObj).every(key => objToCheck.hasOwnProperty(key));
}



app.use(bodyParser.json());

const acc = {
  vorname:"Max",
  nachname:"Mustermann",
  email:"maxmustermann@gmail.com",
} 

// POST request handler
app.post('/api/users', (req, res) => {
  const requestData = req.body;
  if(!hasAllKeys(acc,requestData) ){
    res.status(400).json({message:"Wrong data"})
    return;
  }

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
    res.status(500).json({ error: 'Internal Server Error' });
    return;
  }

  res.status(200).json({ message: 'POST request received successfully' });
});

app.listen(port, () => {
  console.log(`Server is listening at http://localhost:${port}`);
});

app.get('/api/users', (req, res) => {
  try {
    const data = fs.readFileSync('users.json', 'utf-8');
    const jsonData = JSON.parse(data);

    res.status(200).json(jsonData);
  } catch (error) {
    console.error('Error reading users.json:', error);
    res.status(500).json({ error: 'Internal Server Error' });
  }
});
