import React from 'react';
import "./Urlpage.css";
import { useState, useEffect } from 'react';
import axiosInstance from '../apiClient';



export default function Urlpage({ urls,time }) {

  const[Suggestions, setSuggestions] = useState([]);

  const [suggestiontoshow, setsuggestiontoshow] = useState();

  const [showSuggestions, setShowSuggestions] = useState(false);
  const [newurls,setnewurls] = useState(urls);

  const [query, setQuery] = useState();
  const [timetaken, settimetaken] = useState(time);



  const [startIndex, setStartIndex] = useState(0);
  const [nextavailable, setnextavailable] = useState();
  const [prevavailable, setprevavailable] = useState();
  const [ispending ,setispending] = useState(false);

  const showNextArrays = () => {
    setStartIndex(prevIndex => prevIndex + 10);
    setprevavailable(true);
    if(newurls.length - (startIndex) >= 20 ) setnextavailable(true);
    else{setnextavailable(false);}
  };

  const showprevArrays = () => {
    setStartIndex(prevIndex => prevIndex - 10);
    if(startIndex === 10) setprevavailable(false);
    setnextavailable(true);
  };


  const handleChange = (event) => {
    if (event.key === 'Enter') {
      const value = event.target.value; // Get the full value of the input field
      setQuery(value);
      addStringToLocalStorage(value);
    }
  };

  const handleInputChange = (event) => {
    const newQuery = event.target.value;
    getSuggestions(newQuery);
    setShowSuggestions(true);
  };


  const handleSuggestionClick = (suggestion) => {
    setShowSuggestions(false);
    setQuery(suggestion);
  };

  const getSuggestions = (newQuery) => {
    const filteredSuggestions = Suggestions
    .filter((suggestion) => {
      const queryWords = newQuery.toLowerCase().split(' ');
      return queryWords.every(word => suggestion.toLowerCase().includes(word));
    })
    .map((suggestion) => suggestion);

    setsuggestiontoshow(filteredSuggestions);
    setShowSuggestions(true);
  };

  function addStringToLocalStorage(str) {
    let strings = [];
  
    // Get existing strings from localStorage
    const existingStrings = localStorage.getItem('myStrings');
    if (existingStrings) {
      strings = JSON.parse(existingStrings);
    }
  
    // Check if the string is already present
    if (!strings.includes(str)) {
      // Add the new string to the array
      strings.push(str);
  
    // Save the updated array back to localStorage
      localStorage.setItem('myStrings', JSON.stringify(strings));
    }
  };
  
  // Function to get all strings from localStorage as an array
  function getAllStringsFromLocalStorage() {
    const existingStrings = localStorage.getItem('myStrings');
    return existingStrings ? JSON.parse(existingStrings) : [];
  };

  useEffect(() => {
    if(query){
      


    setispending(true);
    const startTime = performance.now(); // Start measuring time
  
    axiosInstance.get('/search', {
      params: {
        keyword: query
      }
    }).then(response => {
      setnewurls(response.data);
      setispending(false);
      const endTime = performance.now(); // Stop measuring time
      const elapsedTime = endTime - startTime; // Calculate elapsed time in milliseconds
      console.log('Time taken:', elapsedTime, 'ms');
      settimetaken(elapsedTime/1000);
    }).catch(error => {
      console.error('Error searching', error);
    });
    }
  },[query]);

  useEffect(() => {
    setprevavailable(false);
    setnextavailable((newurls.length > 10) ? true:false);
    const allStrings = getAllStringsFromLocalStorage();
    setSuggestions(allStrings);
    if(newurls.length===0){
      alert('NO Result!');
    }
  },[newurls]);

  useEffect(() => {
    document.addEventListener('click', handleInputBlur);
    return () => {
      document.removeEventListener('click', handleInputBlur);
    };
  }, []);

  // Function to handle input blur
  const handleInputBlur = (event) => {
    if (!event.target.closest('input') && !event.target.closest('ul')){
      setShowSuggestions(false);
    }
  };

  
  return (
    <div className='urlpage_main'>
      <div  className='urlpage'>
      <input
          className='searchbar'
          type="text"
          placeholder="Type your url"
          onKeyDown={handleChange}
          onChange={handleInputChange}
        />
        {showSuggestions && (
        <ul >
          {suggestiontoshow.map((suggestion, index) => (
            <li key={index} onClick={() => handleSuggestionClick(suggestion)}>
            {suggestion}
          </li>
          ))}
        </ul>
        )}
        <div className='urls'>
        {newurls.length===0 && <p className='notFound'>NOT FOUND</p>}
        {newurls.length!==0 && newurls.slice(startIndex, startIndex + Math.min(10,newurls.length - startIndex)).map((card) => (
          <div className='element'>
            <a className='url' href={card[0]} target="_blank" rel="noopener noreferrer" >{card[0]}</a>
            <div className='describtion'>
              <span>snippet :</span> 
              <div dangerouslySetInnerHTML={{ __html: card[1] }} /></div>
          </div>
        ))}
        </div>
      </div>
      <div className='timetaken'>
        <h3>Timetaken : {timetaken}</h3>
      </div>
      {nextavailable && <button className="next10" onClick={showNextArrays}></button>}
      {prevavailable && <button className="prev10" onClick={showprevArrays}></button>}
      {ispending && <div className="loading"></div>}
    </div>
  )
}
