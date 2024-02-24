const express = require('express');
const bodyParser = require('body-parser');
const fs = require('fs');

const app = express();
const port = 3000;

function hasAllKeys(objToCheck, keysObj) {
  return Object.keys(keysObj).every(key => objToCheck.hasOwnProperty(key));
}


// Middleware to parse JSON in the request body
app.use(bodyParser.json());

const acc = {
  vorname:"Max",
  nachname:"Mustermann",
  email:"maxmustermann@gmail.com",
} 

// POST request handler
app.post('/api/users', (req, res) => {
  const requestData = req.body;
  if(!hasAllKeys(acc,requestData)){
    res.status(400).json({message:"Wrong data"})
    return;
  }

  try {
    const existingData = fs.readFileSync('users.json', 'utf-8');
    const parsedData = JSON.parse(existingData);
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

// Start the server
app.listen(port, () => {
  console.log(`Server is listening at http://localhost:${port}`);
});
