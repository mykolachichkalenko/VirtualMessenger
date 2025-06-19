import './login.css'
import {useEffect, useState} from "react";
import axios from "axios";
export default function CheckAuthenticationForLoginUser({children}){
    const [isAuthenticated, setIsAuthenticated] = useState(null);

    useEffect(() => {
        axios.get("http://localhost:8080/auth/check", {withCredentials: true})
            .then(r => setIsAuthenticated(r.data))
            .catch(err => setIsAuthenticated(false));
    }, []);

    if (isAuthenticated === null) {
        return (
            <div className="justify-center items-center h-screen flex">
                <div className="spinner ">
                    <div></div>
                    <div></div>
                    <div></div>
                    <div></div>
                    <div></div>
                    <div></div>
                    <div></div>
                    <div></div>
                    <div></div>
                    <div></div>
                </div>
            </div>)
    } else if (!isAuthenticated) {
        return children;
    } else {
        window.location.href = "/";
    }
}
