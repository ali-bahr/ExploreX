import React from "react";
import "./BG.css"; 
import bgvideo from "../videos/bg2.mp4";

const BG = () => {
  return (
    <div className="background-video">
      <video autoPlay loop muted className="video">
        <source src={bgvideo} type="video/mp4" />
      </video>
    </div>
  );
};

export default BG;
