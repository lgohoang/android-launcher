var defaultTicket = {
    "ops": [
        { "insert": "-" },
        {
            "attributes": { "align": "center", "header": 1 },
            "insert": "\n"
        },
        {
            "attributes": { "bold": true },
            "insert": "{{TICKET_NUMBER}}"
        },
        {
            "attributes": { "align": "center", "header": 1 },
            "insert": "\n"
        },
        {
            "attributes": { "bold": true },
            "insert": "{{SERVICE}}"
        },
        {
            "attributes": { "align": "center" },
            "insert": "\n"
        },
        {
            "insert": "Printed at: {{TIME}}"
        },
        {
            "attributes": { "align": "center" },
            "insert": "\n"
        }
    ]
};

/////TEMPLATE/////
function makeTemplate(contents) {
    const div = document.createElement("div");
    const quill = new Quill(div);
    quill.setContents(contents);
    //console.log(div);
    return div.firstElementChild.innerHTML;
}

const templates = {}

function register(lang, content) {
    try {
         console.log("content.........", content);
        let data = JSON.parse(content);
        templates[lang] = makeTemplate(data);
    } catch (e) {
        console.error(e);
    }
}

function Register(contents) {
    Object.keys(contents).forEach(lang => {
        register(lang, contents[lang]);
    });
}

function getTemplate(lang) {
    return templates[lang] || templates['en'] || templates["__DEFAULT"];
}


(function() {
    //console.log("default ticket...........");
    let dTicket = JSON.stringify(defaultTicket);
    register("en", dTicket);
    register("__DEFAULT", dTicket);
})();

//Ticket//
const FieldMap = {
    TICKET_NUMBER: "cnum",
    STORE: "store",
    SERVICE: "service",
    SERVICE_NAME: "service_name",
    SERVICE_CODE: "service_code"
}

// {{bithday=Chúc mừng sinh nhật}}
// {{holiday=20/04:Chúc ngày mừng ngày 20-4}}

function CustomerBirthday(t, m) {
    if (!t.customer || !t.customer.date_of_birth) {
        return "";
    }
    try {
        let today = moment().format("MM/DD");
        let _today = today.substring(0, 2) + today.substring(3);
        if (t.customer.date_of_birth.endsWith(_today)) {
            return m;
        } else return "";
    } catch (e) {
        return "";
    }

}

// 20/04=Chúc ngày mừng ngày 20-4
function holidayMessage(config) {
    date = '';
    message = '';
    space = '';
    if (config) {
        parts = config.split("=", 2);
        date = parts[0];
        message = parts[1];
    }
    let today = moment().format("DD/MM");
    if (today.endsWith(date)) {
        return message;
    } else {
        return space;
    }
}

function matchMessage(value, message) {
    if (typeof message !== 'string') {
        return "";
    }
    const parts = message.split("=", 2);
    if (parts[0] === value) {
        return parts[1] || "";
    }
    return "";
}

function fieldFormat(t) {
    return function (key) {
        const parts = key.split(":", 2);
        switch (parts[0]) {
            case "TIME":
                const format = parts[1] || "hh:mm:ss DD/MM/YYYY";
                return moment(t.cdate).format(format);
            case "BIRTHDAY":
                return CustomerBirthday(t, parts[1]);
            case "HOLIDAY":
                return holidayMessage(parts[1]);
            case "SERVICE_CODE":
                return matchMessage(t["service_code"], parts[1]);
            case "$EQ":
                const subs = key.split(":", 2);
                return matchMessage(t[subs[0]], subs[1]);
        }
    }
}

function templateReplacer(t) {
    let formatter = fieldFormat(t);
    return (field) => {
        const key = `${field || ''}`.trim();
        const res = t[FieldMap[key]] || t[key] || formatter(key);
        return res === void 0 ? key : res;
    }
}

function stripHTML(str) {
    const div = document.createElement("div");
    div.innerHTML = str;
    return div.innerText || div.textContent || str;
}

function DisplayTicket(t) {
    const template = getTemplate(t.lang);
    const r = templateReplacer(t);
    const view = template.replace(/{{([^{}]*)}}/g, (str, b) => {
        return r(stripHTML(b));
    })
    return view;
}

function Print(args) {
    var divMain = document.getElementById("main");
    divMain.innerHTML = DisplayTicket(args);
}

function Command(arg) {
    console.log("Command: ", arg);
    const uri = arg.uri;
    const data = arg.data;
    if (!uri) {
        return;
    }
    switch (uri) {
        case "/print-ticket":
            Print(data);
            break;
        case "/set-template":
            Register(data);
            break;
    }
}


