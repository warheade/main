package seedu.addressbook.logic;

import static junit.framework.TestCase.assertEquals;
import static seedu.addressbook.common.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import seedu.addressbook.commands.Command;
import seedu.addressbook.commands.CommandResult;
import seedu.addressbook.commands.ExitCommand;
import seedu.addressbook.commands.HelpCommand;
import seedu.addressbook.commands.player.AddCommand;
import seedu.addressbook.commands.player.AddFastCommand;
import seedu.addressbook.commands.player.ClearCommand;
import seedu.addressbook.commands.player.DeleteCommand;
import seedu.addressbook.commands.player.FindCommand;
import seedu.addressbook.commands.player.ViewAllCommand;
import seedu.addressbook.common.Messages;
import seedu.addressbook.data.AddressBook;
import seedu.addressbook.data.player.Age;
import seedu.addressbook.data.player.Appearance;
import seedu.addressbook.data.player.GoalsAssisted;
import seedu.addressbook.data.player.GoalsScored;
import seedu.addressbook.data.player.HealthStatus;
import seedu.addressbook.data.player.JerseyNumber;
import seedu.addressbook.data.player.Name;
import seedu.addressbook.data.player.Nationality;
import seedu.addressbook.data.player.Player;
import seedu.addressbook.data.player.PositionPlayed;
import seedu.addressbook.data.player.ReadOnlyPlayer;
import seedu.addressbook.data.player.Salary;
import seedu.addressbook.data.player.TeamName;
import seedu.addressbook.data.tag.Tag;
import seedu.addressbook.storage.StorageFile;

public class LogicTest {

    /**
     * See https://github.com/junit-team/junit4/wiki/rules#temporaryfolder-rule
     */
    @Rule
    public TemporaryFolder saveFolder = new TemporaryFolder();

    private StorageFile saveFile;
    private AddressBook addressBook;
    private Logic logic;

    @Before
    public void setup() throws Exception {
        saveFile = new StorageFile(saveFolder.newFile("testSaveFile.txt").getPath());
        addressBook = new AddressBook();
        saveFile.save(addressBook);
        logic = new Logic(saveFile, addressBook);
    }

    @Test
    public void constructor() {
        //Constructor is called in the setup() method which executes before every test, no need to call it here again.

        //Confirm the last shown list is empty
        assertEquals(Collections.emptyList(), logic.getLastPlayerShownList());
    }

    @Test
    public void execute_invalid() throws Exception {
        String invalidCommand = "       ";
        assertCommandBehavior(invalidCommand,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
    }

    /**
     * Executes the command and confirms that the result message is correct.
     * Both the 'address book' and the 'last shown list' are expected to be empty.
     *
     * @see #assertCommandBehavior(String, String, AddressBook, boolean, List)
     */
    private void assertCommandBehavior(String inputCommand, String expectedMessage) throws Exception {
        assertCommandBehavior(inputCommand, expectedMessage, AddressBook.empty(), false, Collections.emptyList());
    }

    /**
     * Executes the command and confirms that the result message is correct and
     * also confirms that the following three parts of the Logic object's state are as expected:<br>
     * - the internal address book data are same as those in the {@code expectedAddressBook} <br>
     * - the internal 'last shown list' matches the {@code expectedLastList} <br>
     * - the storage file content matches data in {@code expectedAddressBook} <br>
     */
    private void assertCommandBehavior(String inputCommand,
                                       String expectedMessage,
                                       AddressBook expectedAddressBook,
                                       boolean isRelevantPlayersExpected,
                                       List<? extends ReadOnlyPlayer> lastPlayerList) throws Exception {

        //Execute the command
        CommandResult r = logic.execute(inputCommand);

        //Confirm the result contains the right data
        assertEquals(expectedMessage, r.feedbackToUser);
        assertEquals(r.getRelevantPlayers().isPresent(), isRelevantPlayersExpected);
        if (isRelevantPlayersExpected) {
            assertEquals(lastPlayerList, r.getRelevantPlayers().get());
        }

        //Confirm the state of data is as expected
        assertEquals(expectedAddressBook, addressBook);
        assertEquals(lastPlayerList, logic.getLastPlayerShownList());
        assertEquals(addressBook, saveFile.load());
    }


    @Test
    public void execute_unknownCommandWord() throws Exception {
        String unknownCommand = "thisisnonsensebutyeahwhocares";
        assertCommandBehavior(unknownCommand, HelpCommand.MESSAGE_ALL_USAGES);
    }

    @Test
    public void execute_help() throws Exception {
        assertCommandBehavior("help", HelpCommand.MESSAGE_ALL_USAGES);
    }

    @Test
    public void execute_exit() throws Exception {
        assertCommandBehavior("exit", ExitCommand.MESSAGE_EXIT_ACKNOWEDGEMENT);
    }

    @Test
    public void execute_clear() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        addressBook.addPlayer(helper.generatePlayer(1));
        addressBook.addPlayer(helper.generatePlayer(2));
        addressBook.addPlayer(helper.generatePlayer(3));
        addressBook.addPlayer(helper.generatePlayer(4));
        assertCommandBehavior("clearPlayer", ClearCommand.MESSAGE_SUCCESS, AddressBook.empty(),
                false, Collections.emptyList());
    }

    @Test
    public void execute_add_invalidArgsFormat() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        // anyhow argument
        assertCommandBehavior(
                "addPlayer wrong args wrong args", expectedMessage);

        //no position prefix
        assertCommandBehavior(
                "addPlayer Valid Name Striker a/30 sal/20000 gs/0 "
                        + "ga/0 tm/validTeam.butNoPositionPrefix ctry/China "
                        + "jn/9 app/0 hs/Healthy", expectedMessage);

        //no age prefix
        assertCommandBehavior(
                "addPlayer Valid Name p/Striker 30 sal/20000 gs/0 "
                        + "ga/0 tm/validTeam.butNoAgePrefix ctry/China "
                        + "jn/9 app/0 hs/Healthy", expectedMessage);

        //no salary prefix
        assertCommandBehavior(
                "addPlayer Valid Name p/Striker a/30 20000 gs/0 "
                        + "ga/0 tm/validTeam.butNoSalaryPrefix ctry/China "
                        + "jn/9 app/0 hs/Healthy", expectedMessage);

        //no goals scored prefix
        assertCommandBehavior(
                "addPlayer Valid Name p/Striker a/30 sal/20000 0 "
                        + "ga/0 tm/validTeam.butNoGoalsScoredPrefix ctry/China "
                        + "jn/9 app/0 hs/Healthy", expectedMessage);

        //no goals assisted prefix
        assertCommandBehavior(
                "addPlayer Valid Name p/Striker a/30 sal/20000 gs/0 "
                        + "0 tm/validTeam.butNoGoalsAssistedPrefix ctry/China "
                        + "jn/9 app/0 hs/Healthy", expectedMessage);

        //no team prefix
        assertCommandBehavior(
                "addPlayer Valid Name p/Striker a/30 sal/20000 gs/0 "
                        + "ga/0 validTeam.butNoPrefix ctry/China "
                        + "jn/9 app/0 hs/Healthy", expectedMessage);

        //no nationality prefix
        assertCommandBehavior(
                "addPlayer Valid Name p/Striker a/30 sal/20000 gs/0 "
                        + "ga/0 tm/validTeam.butNoNationalityPrefix China "
                        + "jn/9 app/0 hs/Healthy", expectedMessage);

        //no jersey number prefix
        assertCommandBehavior(
                "addPlayer Valid Name p/Striker a/30 sal/20000 gs/0 "
                        + "ga/0 tm/validTeam.butNoJerseyNumberPrefix ctry/China "
                        + "9 app/0 hs/Healthy", expectedMessage);

        //no appearance prefix
        assertCommandBehavior(
                "addPlayer Valid Name p/Striker a/30 sal/20000 gs/0 "
                        + "ga/0 tm/validTeam.butNoAppearancePrefix ctry/China "
                        + "jn/9 0 hs/Healthy", expectedMessage);

        //no health status prefix
        assertCommandBehavior(
                "addPlayer Valid Name p/Striker a/30 sal/20000 gs/0 "
                        + "ga/0 tm/validTeam.butNoHealthStatusPrefix ctry/China "
                        + "jn/9 app/0 Healthy", expectedMessage);
    }

    @Test
    public void execute_addFast_invalidArgsFormat() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddFastCommand.MESSAGE_USAGE);

        assertCommandBehavior(
                "addFast ValidName Striker a/30 sal/20000 tm/FC_NUS.butNoPositionPrefix ctry/Singapore jn/10",
                expectedMessage);

        assertCommandBehavior(
                "addFast ValidName p/Striker 30 sal/20000 tm/FC_NUS.butNoAgePrefix ctry/Singapore jn/10",
                expectedMessage);

        assertCommandBehavior(
                "addFast ValidName p/Striker a/30 20000 tm/FC_NUS.butNoSalaryPrefix ctry/Singapore jn/10",
                expectedMessage);

        assertCommandBehavior(
                "addFast ValidName p/Striker a/30 sal/20000 FC_NUS.butNoPrefix ctry/Singapore jn/10",
                expectedMessage);

        assertCommandBehavior(
                "addFast ValidName p/Striker a/30 sal/20000 tm/FC_NUS.butNoNationalityPrefix Singapore jn/10",
                expectedMessage);

        assertCommandBehavior(
                "addFast ValidName p/Striker a/30 sal/20000 tm/FC_NUS.butNoJerseyNumberPrefix ctry/Singapore 10",
                expectedMessage);

    }


    /*@Test
    public void execute_add_invalidPlayerData() throws Exception {
        assertCommandBehavior(
                "addPlayer []\\[;] p/Striker a/30 sal/20000 gs/0 ga/0 tm/validTeam ctry/China"
                        + "jn/9 app/0 hs/Healthy", Name.MESSAGE_NAME_CONSTRAINTS);
        assertCommandBehavior(
                "addPlayer Valid Name p/Striker a/thirty sal/20000 gs/0 ga/0 tm/validTeam ctry/China "
                        + "jn/9 app/0 hs/Healthy", Age.MESSAGE_AGE_CONSTRAINTS);
        assertCommandBehavior(
                "addPlayer Valid Name p/Striker a/30 sal/zero gs/0 ga/0 tm/validTeam ctry/China "
                        + "jn/9 app/0 hs/Healthy", Salary.MESSAGE_SALARY_CONSTRAINTS);
        assertCommandBehavior(
                "addPlayer Valid Name p/Striker a/30 sal/20000 gs/zero ga/0 tm/validTeam ctry/China "
                        + "jn/9 app/0 hs/Healthy", GoalsScored.MESSAGE_GS_CONSTRAINTS);
        assertCommandBehavior(
                "addPlayer Valid Name p/Striker a/30 sal/20000 gs/0 ga/zero tm/validTeam ctry/China "
                        + "jn/9 app/0 hs/Healthy", GoalsAssisted.MESSAGE_GA_CONSTRAINTS);
        assertCommandBehavior(
                "addPlayer Valid Name p/Striker a/30 sal/20000 gs/0 ga/0 tm/validTeam ctry/China "
                        + "jn/50 app/0 hs/Healthy", JerseyNumber.MESSAGE_JN_CONSTRAINTS);
        assertCommandBehavior(
                "addPlayer Valid Name p/Striker a/30 sal/20000 gs/0 ga/0 tm/validTeam ctry/China "
                        + "jn/nine app/0 hs/Healthy", JerseyNumber.MESSAGE_JN_CONSTRAINTS);
        assertCommandBehavior(
                "addPlayer Valid Name p/Striker a/30 sal/20000 gs/0 ga/0 tm/validTeam ctry/China "
                        + "jn/9 app/zero hs/Healthy", Appearance.MESSAGE_APPEARANCE_CONSTRAINTS);
        assertCommandBehavior(
                "addPlayer Valid Name p/Striker a/30 sal/20000 gs/0 ga/0 tm/validTeam ctry/China "
                        + "jn/9 app/0 hs/Healthy t/invalid_-[.tag", Tag.MESSAGE_TAG_CONSTRAINTS);
    }*/

    /*@Test
    public void execute_add_successful() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        Player toBeAdded = helper.messi();
        AddressBook expectedAb = new AddressBook();
        expectedAb.addPlayer(toBeAdded);

        // execute command and verify result
        assertCommandBehavior(helper.generateAddCommand(toBeAdded),
                String.format(AddCommand.MESSAGE_SUCCESS, toBeAdded),
                expectedAb,
                false,
                Collections.emptyList());

    }*/

    /*@Test
    public void execute_addDuplicate_notAllowed() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        Player toBeAdded = helper.messi();
        AddressBook expectedAb = new AddressBook();
        expectedAb.addPlayer(toBeAdded);

        // setup starting state
        addressBook.addPlayer(toBeAdded); // player already in internal address book

        // execute command and verify result
        assertCommandBehavior(
                helper.generateAddCommand(toBeAdded),
                AddCommand.MESSAGE_DUPLICATE_PLAYER,
                expectedAb,
                false,
                Collections.emptyList());

    }*/

    @Test
    public void execute_list_showsAllPersons() throws Exception {
        // prepare expectations
        TestDataHelper helper = new TestDataHelper();
        AddressBook expectedAb = helper.generateAddressBook(2);
        List<? extends ReadOnlyPlayer> expectedList = expectedAb.getAllPlayers().immutableListView();

        // prepare address book state
        helper.addToAddressBook(addressBook, 2);

        assertCommandBehavior("listPlayer",
                Command.getMessageForPlayerListShownSummary(expectedList),
                expectedAb,
                true,
                expectedList);
    }


    /**
     * Confirms the 'invalid argument index number behaviour' for the given command
     * targeting a single player in the last shown list, using visible index.
     *
     * @param commandWord to test assuming it targets a single player in the last shown list based on visible index.
     */
    private void assertInvalidIndexBehaviorForCommand(String commandWord) throws Exception {
        String expectedMessage = Messages.MESSAGE_INVALID_PLAYER_DISPLAYED_INDEX;
        TestDataHelper helper = new TestDataHelper();
        List<Player> lastPlayerList = helper.generatePlayerList(2);

        logic.setLastPlayerShownList(lastPlayerList);

        assertCommandBehavior(commandWord + " -1", expectedMessage, AddressBook.empty(), false, lastPlayerList);
        assertCommandBehavior(commandWord + " 0", expectedMessage, AddressBook.empty(), false, lastPlayerList);
        assertCommandBehavior(commandWord + " 3", expectedMessage, AddressBook.empty(), false, lastPlayerList);
    }

    @Test
    public void execute_viewAll_invalidArgsFormat() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewAllCommand.MESSAGE_USAGE);
        assertCommandBehavior("displayProfile ", expectedMessage);
        assertCommandBehavior("displayProfile arg not number", expectedMessage);
    }

    @Test
    public void execute_viewAll_invalidIndex() throws Exception {
        assertInvalidIndexBehaviorForCommand("displayProfile");
    }

    @Test
    public void execute_tryToViewAllPlayerMissingInAddressBook_errorMessage() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Player p1 = helper.generatePlayer(1);
        Player p2 = helper.generatePlayer(2);
        List<Player> lastPlayerList = helper.generatePlayerList(p1, p2);

        AddressBook expectedAb = new AddressBook();
        expectedAb.addPlayer(p1);

        addressBook.addPlayer(p1);
        logic.setLastPlayerShownList(lastPlayerList);

        assertCommandBehavior("displayProfile 2",
                Messages.MESSAGE_PLAYER_NOT_IN_LEAGUE,
                expectedAb,
                false,
                lastPlayerList);
    }

    @Test
    public void execute_delete_invalidArgsFormat() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE);
        assertCommandBehavior("deletePlayer ", expectedMessage);
        assertCommandBehavior("deletePlayer arg not number", expectedMessage);
    }

    @Test
    public void execute_delete_invalidIndex() throws Exception {
        assertInvalidIndexBehaviorForCommand("deletePlayer");
    }

    @Test
    public void execute_delete_removesCorrectPerson() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Player p1 = helper.generatePlayer(1);
        Player p2 = helper.generatePlayer(2);
        Player p3 = helper.generatePlayer(3);

        List<Player> threePlayers = helper.generatePlayerList(p1, p2, p3);

        AddressBook expectedAb = helper.generateAddressBook(threePlayers);
        expectedAb.removePlayer(p2);

        helper.addToAddressBook(addressBook, threePlayers);
        logic.setLastPlayerShownList(threePlayers);

        assertCommandBehavior("deletePlayer 2",
                String.format(DeleteCommand.MESSAGE_DELETE_PLAYER_SUCCESS, p2),
                expectedAb,
                false,
                threePlayers);
    }

    @Test
    public void execute_delete_missingInAddressBook() throws Exception {

        TestDataHelper helper = new TestDataHelper();
        Player p1 = helper.generatePlayer(1);
        Player p2 = helper.generatePlayer(2);
        Player p3 = helper.generatePlayer(3);

        List<Player> threePlayers = helper.generatePlayerList(p1, p2, p3);

        AddressBook expectedAb = helper.generateAddressBook(threePlayers);
        expectedAb.removePlayer(p2);

        helper.addToAddressBook(addressBook, threePlayers);
        addressBook.removePlayer(p2);
        logic.setLastPlayerShownList(threePlayers);

        assertCommandBehavior("delete 2",
                Messages.MESSAGE_PLAYER_NOT_IN_LEAGUE,
                expectedAb,
                false,
                threePlayers);
    }

    @Test
    public void execute_find_invalidArgsFormat() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        assertCommandBehavior("findPlayer ", expectedMessage);
    }

    @Test
    public void execute_find_onlyMatchesFullWordsInNames() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Player pTarget1 = helper.generatePlayerWithName("bla bla KEY bla");
        Player pTarget2 = helper.generatePlayerWithName("bla KEY bla bceofeia");
        Player p1 = helper.generatePlayerWithName("KE Y");
        Player p2 = helper.generatePlayerWithName("KEYKEYKEY sduauo");

        List<Player> fourPlayers = helper.generatePlayerList(p1, pTarget1, p2, pTarget2);
        AddressBook expectedAb = helper.generateAddressBook(fourPlayers);
        List<Player> expectedList = helper.generatePlayerList(pTarget1, pTarget2);
        helper.addToAddressBook(addressBook, fourPlayers);

        assertCommandBehavior("findPlayer KEY",
                Command.getMessageForPlayerListShownSummary(expectedList),
                expectedAb,
                true,
                expectedList);
    }

    @Test
    public void execute_find_isCaseSensitive() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Player pTarget1 = helper.generatePlayerWithName("bla bla KEY bla");
        Player pTarget2 = helper.generatePlayerWithName("bla KEY bla bceofeia");
        Player p1 = helper.generatePlayerWithName("key key");
        Player p2 = helper.generatePlayerWithName("KEy sduauo");

        List<Player> fourPlayers = helper.generatePlayerList(p1, pTarget1, p2, pTarget2);
        AddressBook expectedAb = helper.generateAddressBook(fourPlayers);
        List<Player> expectedList = helper.generatePlayerList(pTarget1, pTarget2);
        helper.addToAddressBook(addressBook, fourPlayers);

        assertCommandBehavior("findPlayer KEY",
                Command.getMessageForPlayerListShownSummary(expectedList),
                expectedAb,
                true,
                expectedList);
    }

    @Test
    public void execute_find_matchesIfAnyKeywordPresent() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Player pTarget1 = helper.generatePlayerWithName("bla bla KEY bla");
        Player pTarget2 = helper.generatePlayerWithName("bla rAnDoM bla bceofeia");
        Player p1 = helper.generatePlayerWithName("key key");
        Player p2 = helper.generatePlayerWithName("KEy sduauo");

        List<Player> fourPlayers = helper.generatePlayerList(p1, pTarget1, p2, pTarget2);
        AddressBook expectedAb = helper.generateAddressBook(fourPlayers);
        List<Player> expectedList = helper.generatePlayerList(pTarget1, pTarget2);
        helper.addToAddressBook(addressBook, fourPlayers);

        assertCommandBehavior("findPlayer KEY rAnDoM",
                Command.getMessageForPlayerListShownSummary(expectedList),
                expectedAb,
                true,
                expectedList);
    }

    /**
     * A utility class to generate test data.
     */
    class TestDataHelper {
        /**
         * generate a person with the stated parameters
         */
        Player messi() throws Exception {
            Name name = new Name("Lionel Messi");
            PositionPlayed positionPlayed = new PositionPlayed("RW");
            Age age = new Age("30");
            Salary sal = new Salary("2000000");
            GoalsScored goalsScored = new GoalsScored("30");
            GoalsAssisted goalsAssisted = new GoalsAssisted("20");
            TeamName teamName = new TeamName("FC Barcelona");
            Nationality nationality = new Nationality("Argentina");
            JerseyNumber jerseyNumber = new JerseyNumber("10");
            Appearance appearance = new Appearance("54");
            HealthStatus healthStatus = new HealthStatus("Healthy");

            Tag tag1 = new Tag("tag1");
            Tag tag2 = new Tag("tag2");
            Set<Tag> tags = new HashSet<>(Arrays.asList(tag1, tag2));
            return new Player(name, positionPlayed, age, sal, goalsScored, goalsAssisted,
                    teamName, nationality, jerseyNumber, appearance, healthStatus, tags);
        }

        /**
         * Generates a valid player using the given seed.
         * Running this function with the same parameter values guarantees the returned player will have the same state.
         * Each unique seed will generate a unique Person object.
         *
         * @param seed used to generate the player data field values
         */
        Player generatePlayer(int seed) throws Exception {
            return new Player(
                    new Name("Player " + seed),
                    new PositionPlayed("Position" + seed),
                    new Age("" + Math.abs(seed)),
                    new Salary("" + Math.abs(seed)),
                    new TeamName("TeamName" + Math.abs(seed)),
                    new Nationality("Nationality" + Math.abs(seed)),
                    new JerseyNumber("" + (Math.abs(seed) % 35)),
                    new HashSet<>(Arrays.asList(new Tag("tag" + Math.abs(seed)), new Tag("tag" + Math.abs(seed + 1))))
            );
        }

        /**
         * Generates the correct add command based on the player given
         */
        String generateAddCommand(Player p) {
            StringJoiner cmd = new StringJoiner(" ");

            cmd.add("addPlayer");
            cmd.add(p.getName().toString());
            cmd.add(" p/" + p.getPositionPlayed().toString());
            cmd.add(" a/" + p.getAge().toString());
            cmd.add(" sal/" + p.getSalary().toString());
            cmd.add(" gs/" + p.getGoalsScored().toString());
            cmd.add(" ga/" + p.getGoalsAssisted().toString());
            cmd.add(" tm/" + p.getTeamName().toString());
            cmd.add(" ctry/" + p.getNationality().toString());
            cmd.add(" jn/" + p.getJerseyNumber().toString());
            cmd.add(" app/" + p.getAppearance().toString());
            cmd.add(" hs/" + p.getHealthStatus().toString());
            Set<Tag> tags = p.getTags();
            for (Tag t : tags) {
                cmd.add("t/" + t.tagName);
            }
            return cmd.toString();
        }

        /**
         * Generates an AddressBook with auto-generated persons.
         *
         * @param num to indicate the number of player profiles that should be included in the League Tracker.
         */
        AddressBook generateAddressBook(int num) throws Exception {
            AddressBook addressBook = new AddressBook();
            addToAddressBook(addressBook, num);
            return addressBook;
        }

        /**
         * Generates an AddressBook based on the list of Persons given.
         */
        AddressBook generateAddressBook(List<Player> players) throws Exception {
            AddressBook addressBook = new AddressBook();
            addToAddressBook(addressBook, players);
            return addressBook;
        }

        /**
         * Adds auto-generated Person objects to the given AddressBook
         *
         * @param addressBook The AddressBook to which the Persons will be added
         * @param num         to indicate the number of players profiles that should exist in the League Tracker.
         */
        void addToAddressBook(AddressBook addressBook, int num) throws Exception {
            addToAddressBook(addressBook, generatePlayerList(num));
        }

        /**
         * Adds the given list of Persons to the given AddressBook
         */
        void addToAddressBook(AddressBook addressBook, List<Player> playersToAdd) throws Exception {
            for (Player p : playersToAdd) {
                addressBook.addPlayer(p);
            }
        }

        /**
         * Creates a list of Persons based on the give Person objects.
         */
        List<Player> generatePlayerList(Player... players) throws Exception {
            List<Player> playerList = new ArrayList<>();
            for (Player p : players) {
                playerList.add(p);
            }
            return playerList;
        }

        /**
         * Generates a list of Persons based on the number given.
         */
        List<Player> generatePlayerList(int num) throws Exception {
            List<Player> players = new ArrayList<>();
            for (int j = 1; j <= num; j++) {
                players.add(generatePlayer(j));
            }
            return players;
        }

        /**
         * Generates a Person object with given name. Other fields will have some dummy values.
         */
        Player generatePlayerWithName(String name) throws Exception {
            return new Player(
                    new Name(name),
                    new PositionPlayed("Striker"),
                    new Age("25"),
                    new Salary("20000"),
                    new TeamName("FC Barcelona"),
                    new Nationality("Argentina"),
                    new JerseyNumber("10"),
                    Collections.singleton(new Tag("tag"))
            );
        }
    }
}
