exports.config = {
  seleniumAddress: "http://localhost:4444/wd/hub",
  specs: [
      "target/protractor-tests.js"
  ]
};
