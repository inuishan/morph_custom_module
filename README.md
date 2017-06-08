# Custom Code Module
It is easy and convinient to make a chatbot from UI, but that can take us only so far. Almost every serious user have one or the other use case which cannot be built or served from UI and to overcome that shortcoming we have introduced the concept of Custom Code Module. Using Custom Code Module botmakers can write their custom logic private to their organisation and use that at any point in flow execution.

This repo provides skeleton to write custom modules which can be consumed by bot from [UI](https://app.morph.ai/build/flow) .

This repo contains two major sections which any user will need to understand before diving into development.

## BotContext:

This contains execution context of bot at the point when custom module is called.Bot context have four field which are self understood:

1. userVariables  
2. flowVariables  
3. globalVariables  
4. user  

## Action:

This signifies the action the custom module wants to take.
Action have three implementations as of now:

1. **PublishMessageAction**: This can be used to send a message to customer.
2. **SetVariableAction**: This can be used to set any varibale with given Key, Value and VaribleScope (one of the User , Flow and Global).
3. **GoToFlowAction**: This can be used to jump over to any particular flow with given Key along with a flag signifying whether to rerun current flow or not. 

## Example Modules:

As a tutorial (and for easy on boarding) we have added two default module implementations:

1) SamplePublishCustomModule: This published hello to customer.  
2) FetchPersonalDetailFromEmailCustomModule:  This uses email of customer and use that to fetch their details making HTTP call to clearbit.  

> NOTE: To use this you will have to replace field named "\<CLEARBIT_API_KEY>\" in class FetchPersonalDetailFromEmailCustomModule) with your personal key .

## Dependency addition

While making this feature we have tried to give user freedom to use whatever libraries or any third party services they want to use.They can add
maven dependencies in build.gradle.  

> NOTE: only dependencies defined in block `dependencies { ...\<Your Dependecies Go Here\>... }` will be picked

P.S : If you have any suggestion or doubt or even free time you can connect us at [contact@morph.ai](mailto:contact@morph.ai) we will be more than happy to talk with someone else out of our team :) .
