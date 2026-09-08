package darkbook.core;

import com.microsoft.playwright.BrowserContext;

public class StealthManager {

    public static void apply(BrowserContext context){

        context.addInitScript("""
(() => {

Object.defineProperty(navigator,'webdriver',{
    get:()=>undefined
});

Object.defineProperty(navigator,'platform',{
    get:()=>'Win32'
});

Object.defineProperty(navigator,'language',{
    get:()=>'es-ES'
});

Object.defineProperty(navigator,'languages',{
    get:()=>['es-ES','es','en-US']
});

Object.defineProperty(navigator,'hardwareConcurrency',{
    get:()=>8
});

Object.defineProperty(navigator,'deviceMemory',{
    get:()=>8
});

Object.defineProperty(navigator,'plugins',{
    get:()=>[1,2,3,4,5]
});

window.chrome = {
    runtime:{},
    app:{},
    csi:()=>{},
    loadTimes:()=>{}
};

const originalQuery = navigator.permissions.query;

navigator.permissions.query = (parameters)=>
(
parameters.name==='notifications'
? Promise.resolve({state:Notification.permission})
: originalQuery.call(navigator.permissions, parameters)
);

})();
""");

    }

}
