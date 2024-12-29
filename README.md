# ExploreX

Explorex is a powerful search engine that includes a web crawler with robots exclusion protocol, an indexer, and a ranker. It uses the TF-IDF and PageRank algorithms to provide relevant search results.

## Features ✨
- **Phrase Searching**: Find exact phrases within your search queries.
- **Suggestions**: Get search suggestions as you type.
- **Operators**: Use operators to refine your search queries.

## Tech Stack 🛠️
- **Backend**: Java, Spring Boot
- **Frontend**: React
- **Database**: MongoDB

### Prerequisites ✅

- Java 11 or higher
- spring boot
- react and npm
- MongoDB

### Backend Setup ⚙️

1. Clone the repository:

2. create a java project

3. install dependencies

4. run the application file 
```
backendFolder/src/main/java/com/searchengine/Search/SearchApplication.java
```
and replace the ```backendFolder``` with the path of the backend folder on your device after cloning the repo.   
      
### Frontend Setup 🖥️

1. Navigate to the frontend directory:
    ```bash
    cd explorex/frontend
    ```
2. Install dependencies and start the react application
    ```bash
    npm install
    npm start
    ```

### Usage
1. Open your browser and navigate to http://localhost:3000.
2. Enter your search query into the search bar.
3. Use phrase searching by enclosing your query in quotes (e.g., "exact phrase").
4. Use operators like AND, OR, NOT to refine your searches.
5. View search suggestions as you type.
