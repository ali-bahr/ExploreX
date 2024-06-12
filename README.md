# ExploreX

# TODO: a cool logo here

Explorex is a powerful search engine that includes a web crawler with robots exclusion protocol, an indexer, and a ranker. It uses the TF-IDF and PageRank algorithms to provide relevant search results.

## Features

- **Phrase Searching**: Find exact phrases within your search queries.
- **Suggestions**: Get search suggestions as you type.
- **Operators**: Use operators to refine your search queries.

## Tech Stack

- **Backend**: Java, Spring Boot
- **Frontend**: React
- **Database**: MongoDB

## Installation

### Prerequisites

- Java 11 or higher
- Node.js and npm
- MongoDB

### Backend Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/explorex.git
   cd explorex/backend
   ```

2. Install dependencies  
    ?? do we need this or let's just use the .jar files ??
# 3. TODO: Run Crawler, then Indexer, then Ranker 

### Frontend Setup

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
