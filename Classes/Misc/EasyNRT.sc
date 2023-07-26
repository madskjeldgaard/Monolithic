/*
*
* A convenience class that takes care of the nitty gritty of NRT
*
*/

EasyNRT {
  var <server, <format, <>score, <nrtserver, <serverOptions, <path, <nrtduration, <sampleRate, <headerFormat, <lfoSynthDefs, <numChans;

  *new{|numChannels, sampleFormat="int32", header="wav", samplerate=44100, duration=30, outfile, options|
    ^super.new.init(numChannels, sampleFormat, header, samplerate, duration, outfile, options)
  }

  defaultOptions{
    ^ServerOptions.new
      .sampleRate_(sampleRate)
      .memSize_(1024*512) // Allocate 512 mb memory
      .numOutputBusChannels_(numChans)
      .numInputBusChannels_(numChans)
      .maxSynthDefs_(10000)
      .blockSize_(1)
      .numWireBufs_(1000000)
  }

  init{|numChannels, sampleFormat, header, samplerate, duration, outfile, options|

    numChans = numChannels;
    path = outfile;
    nrtduration = duration;
    sampleRate = samplerate;
    format = sampleFormat;
    headerFormat = header;

    serverOptions = options ?? {
      this.defaultOptions
    };

    this.makeServer();

  }

  makeServer{
    nrtserver = Server("%-%-nrt-server".format(this.class.name, Date.getDate.stamp), options: serverOptions)
  }

  play{
    score.play;
  }

  recordNRT{
    score.recordNRT(
      outputFilePath: path,
      headerFormat: headerFormat,
      sampleFormat: format,
      sampleRate: sampleRate,
      options: nrtserver.options,
      duration: nrtduration,
      action: {
        "%: done rendering:\n%".format(this.class.name, path).postln
      }
    );
  }

  // [ synthDefName -> (time: 0.0, args: [\freq, 200], modulation: [\amp -> (synthName: \lfnoise2, args: [\freq, 2])]) ]
  populateScore{|...synthSequence|
    var hoagroup, source, fx;

    // NODE ID's are strictly not necessary here, but we add and use them in case the user wants to play the score which would make the default node ids clash with the users's servers
    var nodeID = 10000;

    synthSequence = synthSequence.flatten;
    // sourceSynthName, sourceSynthArguments, fxSynthName, fxSynthArguments
    score = Score([]);

    // All synths will be enclosed in this group
    score.add([0.0, (
      hoagroup = Group.basicNew(nrtserver, nodeID); hoagroup.newMsg
    )]);

    this.addDefaultLFOS(score);

    synthSequence.do{|synthInfo, index|
      var synthName = synthInfo.key;
      var args = synthInfo.value.args;
      var modulation = synthInfo.value.modulation;
      var time = synthInfo.value.time ? 0.0;
      var synth;

      // Map lfo's
      if(modulation.notNil, {
        modulation.do{|argpair|
          var argName = argpair.key;
          var lfoDict = argpair.value;
          var lfoName = lfoDict.synthName;
          var lfoArgs = lfoDict.args;
          var bus = Bus.audio(nrtserver);

          // Set bus as output
          lfoArgs = lfoArgs ++ [\out, bus];

          // Add bus mapping to synth argument array
          args = args ++ [argName, bus.asMap];

          // Add lfo synth to score
          score.add([time,
            Synth.basicNew(lfoName, server: nrtserver, nodeID: nodeID = nodeID +1 ).newMsg(hoagroup, args: lfoArgs)
          ]);

          "%: Mapping LFO % to arg % of synth %".format(this.class.name, lfoName, argName, synthName).postln;
        }
      });

      // Generate synths
      "%: Adding synth % with args % at index % to NRT Score".format(this.class.name, synthName, args, index).postln;
      if(index == 0, {

        // First synth
        score.add([time,

          synth = Synth.basicNew(
            synthName,
            server: nrtserver,
            nodeID: nodeID = nodeID +1
          ).newMsg(
            hoagroup,
            args:args
          )

        ])

      }, {

        // Add to tail
        score.add([time,

          synth = Synth.basicNew(
            synthName,
            server: nrtserver,
            nodeID: nodeID = nodeID +1
          ).addToTailMsg(
            hoagroup,
            args: args
          )

        ])

      });

    };

      // Add free message to end
      score.add([nrtduration, (hoagroup.freeMsg)]);

    }

    addDefaultLFOS{|score|
      var sources = [
        \sine -> {|freq=1, phase=0| SinOsc.ar(freq:freq, phase:phase) },
        \tri -> {|freq=1, iphase=0| LFTri.ar(freq:freq, iphase:iphase) },
        \saw -> {|freq=1, iphase=0| LFSaw.ar(freq:freq, iphase:iphase) },
        \lfnoise0 -> {|freq=1| LFNoise0.ar(freq:freq) },
        \lfnoise1 -> {|freq=1| LFNoise1.ar(freq:freq) },
        \lfnoise2 -> {|freq=1| LFNoise2.ar(freq:freq) },
        \lfdnoise3 -> {|freq=1| LFDNoise3.ar(freq:freq) },
        \dust -> {|density=1| Dust.ar(density) },
        \dust2 -> {|density=1| Dust2.ar(density) },
        \impulse -> {|freq=1, phase=0| Impulse.ar(freq, phase) }
      ];

      lfoSynthDefs = ();

      sources.do{|source|
        var name = source.key;
        var func = source.value;
        var prefix = this.class.name.toLower;
        var synthDefName = prefix ++ "_" ++ name;

        "%: Adding lfo synthdef %".format(this.class.name, synthDefName).postln;

        synthDefName = synthDefName.asSymbol;

        lfoSynthDefs.put(synthDefName,
          SynthDef(synthDefName, {|out=0, outMin=0.0, outMax=1.0, curve=0, clip='minmax'|
            var sig = SynthDef.wrap(func);

            // Scale and (optionally) warp and (optionally) clip lfo
            sig = sig.lincurve(inMin: -1.0, inMax: 1.0, outMin: outMin, outMax: outMax, curve: curve, clip: clip);

            Out.ar(bus:out, channelsArray:sig)

          })
        )
      };

      lfoSynthDefs.do{|sd|
        score.add([0.0, ["/d_recv", sd.asBytes]])
      }
    }

    lfoNames{
      ^lfoSynthDefs.keys.asArray
    }
}
