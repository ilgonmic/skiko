// see https://kotlinlang.org/docs/js-project-setup.html#webpack-configuration-file


const path = require("path");
const os = require("os")

config.browserConsoleLogOptions.level = "debug";

const basePath = config.basePath;
const projectPath = path.resolve(basePath, "..", "..", "..", "..", "..");
const generatedAssetsPath = path.resolve(projectPath, "build", "karma-webpack-out")
const wasmTestsMjs = path.resolve(basePath, "..", "kotlin", "skiko-kjs-wasm-test.mjs")
const staticLoadMJs = path.resolve(basePath, "..", "static", "load.mjs")
const wasmTestsLoaderWasm = path.resolve(basePath, "..", "kotlin", "load-test.mjs")

const debug = message => console.log(`[karma-config] ${message}`);

debug(`karma basePath: ${basePath}`);
debug(`karma generatedAssetsPath: ${generatedAssetsPath}`);

config.proxies["/resources"] = path.resolve(basePath, "..", "kotlin");

config.preprocessors[wasmTestsLoaderWasm] = ["webpack"];

config.files = config.files.filter((x) => x !== wasmTestsMjs);
config.files = config.files.filter((x) => x !== staticLoadMJs);

config.files = [
    {pattern: path.resolve(generatedAssetsPath, "**/*"), included: false, served: true, watched: false},
    {pattern: path.resolve(basePath, "..", "kotlin", "**/*.png"), included: false, served: true, watched: false},
    {pattern: path.resolve(basePath, "..", "kotlin", "**/*.gif"), included: false, served: true, watched: false},
    {pattern: path.resolve(basePath, "..", "kotlin", "**/*.ttf"), included: false, served: true, watched: false},
    {pattern: path.resolve(basePath, "..", "kotlin", "**/*.txt"), included: false, served: true, watched: false},
    {pattern: path.resolve(basePath, "..", "kotlin", "**/*.json"), included: false, served: true, watched: false},
].concat(config.files);

config.files.push(wasmTestsLoaderWasm);

config.webpack.resolve = {
    alias: {
        skia: false,
        GL: false,
        SkikoCallbacks: false
    },
};

function KarmaWebpackOutputFramework(config) {
    // This controller is instantiated and set during the preprocessor phase.
    const controller = config.__karmaWebpackController;

    // only if webpack has instantiated its controller
    if (!controller) {
        console.warn(
            "Webpack has not instantiated controller yet.\n" +
            "Check if you have enabled webpack preprocessor and framework before this framework"
        )
        return
    }

    config.files.push({
        pattern: `${controller.outputPath}/**/*`,
        included: false,
        served: true,
        watched: false
    })
}

const KarmaWebpackOutputPlugin = {
    'framework:webpack-output': ['factory', KarmaWebpackOutputFramework],
};

config.plugins.push(KarmaWebpackOutputPlugin);
config.frameworks.push("webpack-output");

// New opcodes only in Canary
config.browsers = ["ChromeCanaryHeadlessWasmGc"];
config.customLaunchers = {
    ChromeCanaryHeadlessWasmGc: {
        base: 'ChromeCanaryHeadless',
        flags: ['--js-flags=--experimental-wasm-gc']
    }
};