import React from "react"

import "./assets/css/bootstrap.min.css"
import "./assets/css/styles.css"
import MainNavBar from "./component/MainNavBar"
import MainView from "./component/MainView"

const App = () => {
    return (
        <div>
            <MainNavBar />
            <MainView />
        </div>
    );
};

export default App
