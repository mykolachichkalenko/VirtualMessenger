import Chat from "./componentsForMainPage/Chat.jsx";
import UsersSearchETC from "./componentsForMainPage/UsersSearchETC.jsx";
import {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import axios from "axios";

export default function MainPage(){
    const {phone} = useParams();
    const [myPhone, setMyPhone] = useState("");

    useEffect(() => {
        axios.get("http://localhost:8080/get/my/phone", {withCredentials: true})
            .then(resp => setMyPhone(resp.data));

        axios.get("http://localhost:8080/get/set/online",{withCredentials:true}).then();
    }, []);
    return(
        <div className={"h-screen w-screen flex justify-center items-center"}>

            <div className={" bg-white w-screen h-screen flex justify-items-center"}>
                <div className="h-screen bg-danger w-screen lg:w-1/2 2xl:w-1/4">
                    {/*Chats,etc.*/}
                    <UsersSearchETC myPhone={myPhone}/>
                </div>
                {/*Chat With User*/}
                <Chat/>

            </div>
        </div>
    )
}