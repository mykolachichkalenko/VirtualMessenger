import axios from "axios";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faCheck} from "@fortawesome/free-solid-svg-icons";
import { faPaypal } from "@fortawesome/free-brands-svg-icons";
import {useEffect} from "react";

export default function Premium() {

    useEffect(() => {
        axios.get("http://localhost:8080/get/get/me",{withCredentials:true})
            .then(response =>{
                if (response.data.isPremium){
                    window.location.href="/";
                }
            })
    }, []);
    const handlePayment = async (e) => {

        const response = await axios.post("http://localhost:8080/paypal/create",
            {}, {withCredentials: true});

        if (response.data.approvalUrl){
            window.location.href=response.data.approvalUrl;
        }
    }

    const testBuying = () =>{
        axios.get("http://localhost:8080/test/pay",{withCredentials:true}).then(res => window.location.href="/");
    }
    return (
        <div
            className="w-screen h-screen bg-gradient-to-br from-[#0d0d0d] to-black text-white flex items-center justify-center px-4">
            <div
                className="bg-[#111111] text-gray-400 rounded-3xl p-8 shadow-2xl w-full max-w-md space-y-6 border border-gray-800 animate-fade-in">
                <div className="text-center space-y-1">
                    <h2 className="text-4xl font-extrabold text-white tracking-tight">VM Premium</h2>
                    <p className="text-gray-500 text-sm">Get the best from Virtual Messenger</p>
                </div>


                <ul className="space-y-4">
                    {[
                        "AI Integrated",
                        "Correct your message with AI",
                        "Translate your message with AI",
                        "Get 2x faster experience",
                    ].map((feature, index) => (
                        <li key={index} className="flex items-center gap-3 text-lg">
              <span className="text-blue-500">
                <FontAwesomeIcon icon={faCheck}/>
              </span>
                            <span>{feature}</span>
                        </li>
                    ))}
                </ul>

                <div className="text-center space-y-3 pt-4">
                    <p className="text-2xl font-semibold text-white">$10 / month</p>
                    <button
                        onClick={handlePayment}
                        className="bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 px-6 rounded-xl w-full transition duration-300 flex items-center justify-center gap-2"
                    >
                        <FontAwesomeIcon icon={faPaypal} className="text-xl"/>
                        Buy with PayPal
                    </button>
                </div>
            </div>
            <button className={"absolute top-40"} onClick={testBuying}>test buying</button>
        </div>
    )
}