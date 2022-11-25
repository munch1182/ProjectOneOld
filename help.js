const fs = require('fs');
const path = require('path');

function emptyDir(dir) {
    if (!fs.existsSync(dir)) {
        return;
    }
    for (const f of fs.readdirSync(dir)) {
        const abs = path.resolve(dir, f);
        if (fs.lstatSync(abs).isDirectory()) {
            emptyDir(abs);
            fs.rmdirSync(abs);
        } else {
            fs.unlinkSync(abs);
        }
    }
    return;
}

module.exports = { emptyDir }