import axios from "axios";
import {useEffect, useState} from "react";
import BlurText from "../designedComponents/BlurText.jsx";
import Lightning from "../designedComponents/BackGround.jsx";
import {Alert, Button, Form, Input, InputOtp} from "@heroui/react";
import ShinyText from "../designedComponents/ShineText.jsx";
import {AnimatePresence, motion} from "framer-motion";
import GlareHover from "../designedComponents/GlareHover.jsx";

export default function PhoneAuthentication() {
    const [phone, setPhone] = useState("");
    const [isCorrectPhoneNumber, setIsCorrectPhoneNumber] = useState(false);
    const [isPhoneSend, setIsPhoneSend] = useState(false);
    const [isPhoneSendForAnimation, setIsPhoneSendForAnimation] = useState(false);
    const [code, setCode] = useState("");
    const [isCorrectCode, setIsCorrectCode] = useState(false);
    const [isIssueWithCode,setIsIssueWithCode] = useState(false);

    const handlePhoneChange = (event) => {
        const value = event.target.value;
        if (/^\d*$/.test(value)) {
            setPhone(value);
        }
    };
    //code checking
    useEffect(() => {
        if (code.length === 6) {
            setIsCorrectCode(true);
        } else {
            setIsCorrectCode(false);
        }
    }, [code]);
    //phone checking
    useEffect(() => {
        console.log(phone)
        if (phone) {
            if (phone.length === 12) {
                setIsCorrectPhoneNumber(true);
            } else {
                setIsCorrectPhoneNumber(false);
            }
        } else {
            setIsCorrectPhoneNumber(false);
        }
    }, [phone]);

    //for animation
    useEffect(() => {
        if (isPhoneSend) {
            setIsPhoneSendForAnimation(true)
            const timer = setTimeout(() => setIsPhoneSendForAnimation(false), 3000);
            return () => clearTimeout(timer);
        }
    }, [isPhoneSend]);
    useEffect(() => {
        if (isIssueWithCode){
            setCode("")
            const timer = setTimeout(() => setIsIssueWithCode(false),3000);
            return () => clearTimeout(timer);
        }
    }, [isIssueWithCode]);

    const sendPhoneForCode = (e) => {
        e.preventDefault();
        axios.post("http://localhost:8080/auth/request-otp", {phone: phone},
            {withCredentials: true}).then(r => setIsPhoneSend(true));
    }

    const sendCode = () => {
        axios.post("http://localhost:8080/auth/verify-otp", {
            phone: phone,
            code: code
        }, {withCredentials: true}).then(r => window.location.reload())
            .catch(err => setIsIssueWithCode(true));
    }

    return (
        <div className="flex items-center justify-center w-screen h-screen ">
            <Lightning
                hue={220}
                xOffset={0}
                speed={1}
                intensity={1}
                size={1}
            />
            <BlurText
                text="Welcome to VM"
                delay={50}
                animateBy="letters"
                className="text-9xl max-lg:text-8xl max-md:text-7xl max-sm:text-5xl text-gray-900/80 font-black text-center absolute top-20 max-lg:top-30 max-md:top-40"

            />
            <motion.div
                initial={{opacity: 0, y: 200}}
                animate={{opacity: 1, y: 0}}
                transition={{duration: 0.5}}
                className="w-[400px] h-[500px] rounded-2xl border-1
                 border-gray-400 bg-transparent backdrop-blur-lg absolute mb-75 justify-center flex">
                <GlareHover
                    glareColor="#ffffff"
                    glareOpacity={0.3}
                    glareAngle={-30}
                    glareSize={300}
                    transitionDuration={800}
                    playOnce={false}>
                    <ShinyText text="REGISTRATION" disabled={false} speed={3}
                               className='custom-class absolute text-4xl top-5 font-bold'/>
                    <AnimatePresence mode={"wait"}>
                        {isPhoneSend ?
                            <>
                                <motion.div
                                    key={"codeForm"}
                                    initial={{opacity: 0, x: 50}}
                                    animate={{opacity: 1, x: 0}}
                                    transition={{duration: 0.8}}
                                    className="flex flex-col items-start gap-3 relative top-0">
                                    <ShinyText text={"enter the code"} className={"relative top-4"}></ShinyText>
                                    <InputOtp length={6} value={code} onValueChange={setCode}/>
                                    <Button className={"relative bottom-3 left-32"} variant="bordered"
                                            disabled={!isCorrectCode} onClick={sendCode}>Send the code</Button>
                                </motion.div>
                                <AnimatePresence>
                                    {isPhoneSendForAnimation &&
                                        <motion.div className={"absolute top-80"}
                                                    key={"AlertSuccess"}
                                                    initial={{opacity: 0, x: 100}}
                                                    animate={{opacity: 1, x: 0}}
                                                    transition={{duration: 0.3}}
                                                    exit={{opacity: 0, x: 100}}>
                                            <Alert color={"success"} title={`we sent the code to +${phone}`}/>
                                        </motion.div>}
                                    {isIssueWithCode &&
                                        <motion.div
                                            className={"absolute top-80"}
                                        key={"AlertError"}
                                        initial={{opacity: 0, x: 100}}
                                        animate={{opacity: 1, x: 0}}
                                        transition={{duration: 0.3}}
                                        exit={{opacity: 0, x: 100}}>
                                            <Alert color={"danger"} title={`not correct code`}/>
                                        </motion.div>}
                                </AnimatePresence>
                            </>
                            :
                            <motion.div
                                key={"phoneForm"}
                                className="w-full max-w-xs relative mt-0"
                                initial={{opacity: 0, x: 50}}
                                animate={{opacity: 1, x: 0}}
                                transition={{duration: 1}}
                                exit={{opacity: 0, x: -50}}>
                                <Form className="w-full max-w-xs relative mt-0" onSubmit={sendPhoneForCode}>
                                    <Input
                                        isRequired
                                        errorMessage="Please enter a valid phone"
                                        label="Phone"
                                        labelPlacement="outside"
                                        name="number"
                                        placeholder="Enter your phone number"
                                        type="tel"
                                        value={phone}
                                        onChange={handlePhoneChange}
                                    />
                                    <Button type="submit" variant="bordered" disabled={!isCorrectPhoneNumber}>
                                        Submit
                                    </Button>
                                </Form>
                            </motion.div>}

                    </AnimatePresence>
                </GlareHover>
            </motion.div>
        </div>
    )
}
