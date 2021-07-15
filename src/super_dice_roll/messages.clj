(ns super-dice-roll.messages)

(def help-header
  (str "Available commands: `/roll`, `/history` or `/help`\n"))

(def help-roll
  (str "`/roll <NDM>`\n"
       "You must specify dice and modifiers in following format:\n"
       "N = Number of dices\n"
       "D = Dice type (D6, D12, D20)\n"
       "M = Modifiers (+1, -3)\n"
       "Example: `/roll 3D6+3`\n"))

(def help-history
  (str "`/history`\n"
       "Lists your lasts 10 rolls with the results.\n"))
