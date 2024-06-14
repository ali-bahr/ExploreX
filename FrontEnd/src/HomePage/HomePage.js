import React, { useEffect } from 'react'
import "./HomePage.css";
import { useState } from 'react';
import BG from "../Bg/BG";
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../apiClient';



export default function HomePage({ seturls, settime }) {

    const navigate = useNavigate();

    const [query, setQuery] = useState();

    const [ispending, setispending] = useState(false);

    

    const[Suggestions, setSuggestions] = useState([]);

    const [suggestiontoshow, setsuggestiontoshow] = useState();

    const [showSuggestions, setShowSuggestions] = useState(false);

    const handleChange = (event) => {
      if (event.key === 'Enter') {
        const value = event.target.value; // Get the full value of the input field
        console.log(value);
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
      console.log(str+"from add ftring");
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

    useEffect( () =>{
      const allStrings = getAllStringsFromLocalStorage();
      console.log(allStrings);
      console.log("testing");
      setSuggestions(allStrings);
    },[]);

    useEffect(() => {
    if(query){
      

      setispending(true);
      const startTime = performance.now(); // Start measuring time

      axiosInstance.get('/search', {
        params: {
          keyword: query
        }
      }).then(response => {
        seturls(response.data);
        setispending(false);
        const endTime = performance.now(); // Stop measuring time
        const elapsedTime = endTime - startTime; // Calculate elapsed time in milliseconds
        console.log('Time taken:', elapsedTime, 'ms');
        settime(elapsedTime/1000);
        console.log(query);
        console.log(response.data);
        navigate('/urlpage');
      }).catch(error => {
        console.error('Error searching', error);
      });
     }
    }, [query]) ;

  
    // Function to handle input blur
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
    <div className='homepage'>
        <div className='dark'/>
        <BG/>
        <div className='name'>
            ExploreX
        </div>
        <input
        className='searchbar'
        type="text"
        placeholder="Type your url"
        onKeyDown={handleChange}
        onChange={handleInputChange}
      />
      {showSuggestions && (
        <ul>
          {suggestiontoshow.map((suggestion, index) => (
            <li key={index} onClick={() => handleSuggestionClick(suggestion)} >
              {suggestion}
            </li>
          ))}
        </ul>
      )}
      {ispending && <div className="loading"></div>}
    </div>
  )
}
