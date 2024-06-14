import React from 'react';
import { FaFacebook, FaTwitter, FaInstagram, FaLinkedin } from 'react-icons/fa';
import './Footer.css';
import { useLocation } from 'react-router-dom';



function Footer() {

  const location = useLocation();
  const isHomePage = location.pathname === '/';

  

  return (
    <footer className={isHomePage? "footer":"footer bgcolor textcolor"}>
      <div className="footer-container">
        <div className="footer-logo">ExploreX</div>
        <div className="footer-social-icons">
          <a className={isHomePage? "link":"link textcolor"} href="https://www.facebook.com"><FaFacebook /></a>
          <a className={isHomePage? "link":"link textcolor"} href="https://www.twitter.com"><FaTwitter /></a>
          <a className={isHomePage? "link":"link textcolor"} href="https://www.instagram.com"><FaInstagram /></a>
          <a className={isHomePage? "link":"link textcolor"} href="https://www.linkedin.com"><FaLinkedin /></a>
        </div>
      </div>
    </footer>
  );
}

export default Footer;