import {BrowserRouter, Route, Routes} from "react-router-dom";
import PhoneAuthentication from "./components/login/PhoneAuthentication.jsx";
import CheckAuthenticationForLoginUser from "./components/login/CheckAuthenticationForLoginUser.jsx";
import CheckAuthenticationForNotLoginUser from "./components/login/CheckAuthenticationForNotLoginUser.jsx";
import MainPage from "./components/MainPage/MainPage.jsx";
import MainPageWithChat from "./components/MainPageWithChat/MainPageWithChat.jsx";
import Premium from "./PremiumBuying/Premium.jsx";
import Profile from "./components/Profile.jsx";

function App() {

    return (
        <BrowserRouter>
            <Routes>
                <Route path={"/authentication"} element={<CheckAuthenticationForLoginUser><PhoneAuthentication/></CheckAuthenticationForLoginUser>}/>
                <Route path={"/"} element={<CheckAuthenticationForNotLoginUser><MainPage/></CheckAuthenticationForNotLoginUser>}/>
                <Route path={"/:phone"} element={<CheckAuthenticationForNotLoginUser><MainPageWithChat/></CheckAuthenticationForNotLoginUser>}/>
                <Route path={"/premium"} element={<Premium/>}/>
                <Route path={"/profile"} element={<Profile/>}/>
            </Routes>
        </BrowserRouter>
    )
}

export default App
