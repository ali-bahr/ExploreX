import { BrowserRouter, Route, Routes } from "react-router-dom";
import HomePage from "./HomePage/HomePage";
import Footer from "./Footer/Footer";
import Urlpage from "./UrlPage/Urlpage";
import { useState } from "react";


function App() {

  const [urls,seturls] = useState([]);
  const [time,settime] = useState();

  return (
    <BrowserRouter>
      <Routes>
        <Route exact path="/" element={<HomePage seturls={seturls} settime={settime} />} />
        <Route exact path="/urlpage" element={<Urlpage urls={urls} time={time} />} />
      </Routes>
      <Footer />
    </BrowserRouter>
  );
}

export default App;
