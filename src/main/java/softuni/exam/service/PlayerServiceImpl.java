package softuni.exam.service;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.domain.dtos.PlayerSeedDto;
import softuni.exam.domain.entities.Picture;
import softuni.exam.domain.entities.Player;
import softuni.exam.domain.entities.Team;
import softuni.exam.repository.PlayerRepository;
import softuni.exam.util.ValidatorUtil;

import javax.transaction.Transactional;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static softuni.exam.constants.GlobalConstants.PLAYERS_FILE_PATH;

@Service
@Transactional
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final TeamService teamService;
    private final PictureService pictureService;
    private final ModelMapper modelMapper;
    private final ValidatorUtil validatorUtil;
    private final Gson gson;

    public PlayerServiceImpl(PlayerRepository playerRepository,
                             TeamService teamService,
                             PictureService pictureService,
                             ModelMapper modelMapper,
                             ValidatorUtil validatorUtil,
                             Gson gson) {
        this.playerRepository = playerRepository;
        this.teamService = teamService;
        this.pictureService = pictureService;
        this.modelMapper = modelMapper;
        this.validatorUtil = validatorUtil;
        this.gson = gson;
    }

    @Override
    public String importPlayers() throws FileNotFoundException {
        StringBuilder stringBuilder = new StringBuilder();

        PlayerSeedDto[] playerSeedDtos = this.gson
                .fromJson(new FileReader(PLAYERS_FILE_PATH), PlayerSeedDto[].class);

        Arrays.stream(playerSeedDtos)
                .forEach(playerSeedDto -> {
                    if (this.playerRepository
                            .findFirstByFirstNameAndLastName(playerSeedDto.getFirstName(),
                                    playerSeedDto.getLastName()) != null) {
                        stringBuilder.append("Already in DB.");
                        return;
                    }

                    if (this.validatorUtil.isValid(playerSeedDto)) {
                        if (this.teamService.getTeamByName(playerSeedDto.getTeam().getName()) != null) {
                            if (this.pictureService.getPictureByUrl(playerSeedDto.getPicture().getUrl()) != null) {
                                Player player = this.modelMapper
                                        .map(playerSeedDto, Player.class);

                                Team team = this.teamService.getTeamByName(playerSeedDto.getTeam().getName());

                                Picture picture = this.pictureService.getPictureByUrl(playerSeedDto.getPicture().getUrl());

                                player.setTeam(team);
                                player.setPicture(picture);

                                this.playerRepository.saveAndFlush(player);

                                stringBuilder.append(String.format("Successfully imported player: %s %s",
                                        playerSeedDto.getFirstName(), playerSeedDto.getLastName()));
                            } else {
                                stringBuilder.append("Picture doesn't exists.");
                            }
                        } else {
                            stringBuilder.append("Team doesn't exists.");
                        }
                    } else {
                        stringBuilder.append("Invalid player");
                    }

                    stringBuilder.append(System.lineSeparator());
                });

        return stringBuilder.toString().trim();
    }

    @Override
    public boolean areImported() {
        return this.playerRepository.count() > 0;
    }

    @Override
    public String readPlayersJsonFile() throws IOException {
        return Files.readString(Path.of(PLAYERS_FILE_PATH));
    }

    @Override
    public String exportPlayersWhereSalaryBiggerThan() {
        StringBuilder stringBuilder = new StringBuilder();

        this.playerRepository
                .findAllBySalaryGreaterThanOrderBySalaryDesc(BigDecimal.valueOf(100000))
                .forEach(player -> {
                    stringBuilder.append(String.format("Player name: %s %s \n" +
                            "\tNumber: %d\n" +
                            "\tSalary: %.2f\n" +
                            "\tTeam: %s\n",
                            player.getFirstName(), player.getLastName(),
                            player.getNumber(),
                            player.getSalary(),
                            player.getTeam().getName()))
                            .append(System.lineSeparator());
                });

        return stringBuilder.toString().trim();
    }

    @Override
    public String exportPlayersInATeam() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Team: North Hub")
                .append(System.lineSeparator());

        this.playerRepository
                .findAllByTeamNameOrderById("North Hub")
                .forEach(player -> {
                    stringBuilder.append(String.format("\tPlayer name: %s %s" +
                            " - %s\n" +
                            "\tNumber: %d",
                            player.getFirstName(), player.getLastName(),
                            player.getPosition(),
                            player.getNumber()))
                            .append(System.lineSeparator());
                });

        return stringBuilder.toString().trim();
    }

    @Override
    public Player getPlayerByFirstAndLastName(String firstName, String lastName) {
        return this.playerRepository.findFirstByFirstNameAndLastName(firstName, lastName);
    }
}
